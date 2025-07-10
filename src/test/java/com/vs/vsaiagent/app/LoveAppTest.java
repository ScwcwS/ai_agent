package com.vs.vsaiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.print.DocFlavor;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class LoveAppTest {


    @Resource
    private LoveApp loveApp;
    @Test
    void doChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好我是大侠";
        String answer = loveApp.doChat(message,chatId);
        //第二轮
        message = "怎么谈恋爱";
        answer = loveApp.doChat(message,chatId);
        // 第三轮
        message = "我叫什么名字";
        answer = loveApp.doChat(message,chatId);
    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好我是大侠,怎么让另一半更爱我";
        LoveApp.LoveReport answer = loveApp.doChatWithReport(message,chatId);
        Assertions.assertNotNull(answer);

    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好我是大侠,怎么让另一半更爱我";
        String answer = loveApp.doChatWithRag(message,chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithTools() {
        // 测试联网搜索问题的答案
        testMessage("帮我搜索星空的图片");
//        testMessage("周末想带女朋友去上海约会，推荐几个适合情侣的小众打卡地？");
//
//        // 测试网页抓取：恋爱案例分析
//        testMessage("最近和对象吵架了，看看编程导航网站（codefather.cn）的其他情侣是怎么解决矛盾的？");
//
//        // 测试资源下载：图片下载
//        testMessage("直接下载一张适合做手机壁纸的星空情侣图片为文件");
//
//        // 测试终端操作：执行代码
//        testMessage("执行 Python3 脚本来生成数据分析报告");
//
//        // 测试文件操作：保存用户档案
//        testMessage("保存我的恋爱档案为文件");
//
//        // 测试 PDF 生成
//        testMessage("生成一份‘七夕约会计划’PDF，包含餐厅预订、活动流程和礼物清单");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithMcp() {
//        String chatId = UUID.randomUUID().toString();
//        // 测试高德地图 MCP
//        String answer = loveApp.doChatWithMcp("我的另一半居住在上海静安区，请帮我找到 5公里内合适的约会地点", chatId);
//        Assertions.assertNotNull(answer);

        String chatId = UUID.randomUUID().toString();
        // 测试本地 MCP
        String answer = loveApp.doChatWithMcp("帮我搜索铠甲勇士的图片", chatId);
        Assertions.assertNotNull(answer);
    }
}