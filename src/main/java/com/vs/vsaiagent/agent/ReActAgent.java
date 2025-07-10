package com.vs.vsaiagent.agent;


import cn.hutool.core.collection.CollUtil;
import com.vs.vsaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * 基于ReAct
 * 思考 与 行动
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class ReActAgent extends BaseAgent {

    /**
     *  思考
     * @return 是否需要执行动作
     */
    public abstract boolean think();


    /**
     * 行动
     * @return 动作结果
     */
    public abstract String act();

    /**
     * 执行单个步骤：思考和行动
     * @return 步骤执行结果
     */
    @Override
    public String step() {
        try {
            if (think()) {
                return act();
            }
            setState(AgentState.FINISHED);
            // getMessageList 得到最后的消息回复
            return CollUtil.getLast(getMessageList()).getText();
        } catch (Exception e) {
            e.printStackTrace();
            return "步骤执行失败 " + e.getMessage();
        }


    }

}
