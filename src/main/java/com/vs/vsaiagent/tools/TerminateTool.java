package com.vs.vsaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;

/**
 * 终止工具（作用是让自主规划智能体能够合理地中断）
 */
public class TerminateTool {

    @Tool(description = """
            Terminate the interaction when the request is met OR if the assistant cannot proceed further with the task.
            After completing all tasks, you will call this tool to finish the work.
            """)
    public String doTerminate() {
        return "任务结束";
    }
}
