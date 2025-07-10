package com.vs.vsaiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.vs.vsaiagent.agent.model.AgentState;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.util.StringUtil;
import org.apache.catalina.User;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 基类 Agent，用于管理 Agent 的状态
 * 提供状态转换，内存管理，循环执行
 */
@Data
@Slf4j
public abstract class BaseAgent {


    // Agent name
    private String name;

    // prompt

    private String systemPrompt;

    // next step prompt
    private String nextStepPrompt;

    // agent status
    private AgentState state = AgentState.IDLE;

    // 当前步数
    private int currentStep = 0;

    // 最大步数
    private int maxSteps = 10;

    // LLM 大模型
    private ChatClient chatClient;

    // Memory 自己维护对话上下文
    private List<Message> messageList = new ArrayList<>();

    /**
     *  执行 agent
     * @param userPrompt
     * @return
     */
    public String run(String userPrompt) {
        // 基础校验
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }

        // 更改状态
        this.state = AgentState.RUNNING;
        // 记录message上下文
        messageList.add(new UserMessage(userPrompt));
        // 保存结果到列表
        ArrayList<String> results = new ArrayList<>();
        try{
            for (int i = 0; i < maxSteps && this.state != AgentState.FINISHED; i ++) {
                currentStep = i + 1;
                log.info("Executing step: {}/{}", currentStep,maxSteps);
                String stepResult = step();
                String resutl = "Step " + currentStep + ": " + stepResult;
                results.add(resutl);
            }
            if (currentStep >= maxSteps) {
                this.state = AgentState.FINISHED;
                results.add("Terminated Reached max step (" + maxSteps + ")");
            }
            return String.join("\n",results);
        } catch (Exception e){
            state = AgentState.ERROR;
            log.error("error executing agent",e);
            return "执行结果错误" + e.getMessage();
        }
        finally {
            this.cleanup();
        }


    }
    /**
     * 运行代理（流式输出）
     *
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public SseEmitter runStream(String userPrompt,String contentText) {
        // 判断字符串是否为空
        if (!contentText.isEmpty()) {
            // 添加上下文
            messageList.add(new UserMessage("这是之前的一些对话记录" + contentText));
        }

        // 创建一个超时时间较长的 SseEmitter
        SseEmitter sseEmitter = new SseEmitter(300000L); // 5 分钟超时
        // 使用线程异步处理，避免阻塞主线程
        CompletableFuture.runAsync(() -> {
            // 1、基础校验
            try {
                if (this.state != AgentState.IDLE) {
                    sseEmitter.send("错误：无法从状态运行代理：" + this.state);
                    sseEmitter.complete();
                    return;
                }
                if (StrUtil.isBlank(userPrompt)) {
                    sseEmitter.send("错误：不能使用空提示词运行代理");
                    sseEmitter.complete();
                    return;
                }
            } catch (Exception e) {
                sseEmitter.completeWithError(e);
            }
            // 2、执行，更改状态
            this.state = AgentState.RUNNING;
            // 记录消息上下文
            messageList.add(new UserMessage(userPrompt));
            // 保存结果列表
            List<String> results = new ArrayList<>();
            try {
                // 执行循环
                for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                    int stepNumber = i + 1;
                    currentStep = stepNumber;
                    log.info("Executing step {}/{}", stepNumber, maxSteps);
                    // 单步执行
                    String stepResult = step();
                    String result = "Step " + stepNumber + ": " + stepResult;
                    results.add(result);
                    // 输出当前每一步的结果到 SSE
                    // 随机范围2-6个字符大小
                    int chunkSize = 0;
                    for (int j = 0;j < result.length(); j += chunkSize) {
                        chunkSize = ThreadLocalRandom.current().nextInt(1, 7);
                        String chunk = result.substring(j, Math.min(j + chunkSize, result.length()));
                        sseEmitter.send(chunk);
                        // 添加少量延迟，让前端有时间处理
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            this.state = AgentState.FINISHED;
                            sseEmitter.send("系统出错！！！");
                        }
                    }
                    sseEmitter.send("\n");
                }
                // 检查是否超出步骤限制
                if (currentStep >= maxSteps) {
                    state = AgentState.FINISHED;
                    results.add("Terminated: Reached max steps (" + maxSteps + ")");
                    sseEmitter.send("执行结束：达到最大步骤（" + maxSteps + "）");
                }
                // 正常完成
                sseEmitter.complete();
            } catch (Exception e) {
                state = AgentState.ERROR;
                log.error("error executing agent", e);
                try {
                    sseEmitter.send("执行错误：" + e.getMessage());
                    sseEmitter.complete();
                } catch (IOException ex) {
                    sseEmitter.completeWithError(ex);
                }
            } finally {
                // 3、清理资源
                this.cleanup();
            }
        });

        // 设置超时回调
        sseEmitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.cleanup();
            log.warn("SSE connection timeout");
        });
        // 设置完成回调
        sseEmitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED;
            }
            this.cleanup();
            log.info("SSE connection completed");
        });
        return sseEmitter;
    }

    /**
     * 定义单个步骤
     * @return
     */
    public abstract String step ();

    /**
     * 清理资源
     */
    protected void cleanup() {}

}
