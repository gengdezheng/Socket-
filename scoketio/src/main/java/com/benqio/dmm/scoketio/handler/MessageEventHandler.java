package com.benqio.dmm.scoketio.handler;

import com.alibaba.fastjson.JSON;
import com.benqio.dmm.scoketio.message.SocketMessage;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
@Log4j2
public class MessageEventHandler {

    @Resource
    private SocketMessage socketMessage;

    private final SocketIOServer server;
    //保存已连接的客户端,用于客户端推送
    private Map<String, SocketIOClient> clients = new HashMap<String, SocketIOClient>();
    //保存controlRoomId，用于服务器端推送
    private Set<String> controlRoomIds = new HashSet<>();
    public static final String CONTROL_ROOM_ID = "controlRoomId";
    public static final String CLIENT_ID = "clientId";
    public static final String ALL = "all";

    @Autowired
    public MessageEventHandler(SocketIOServer server) {
        this.server = server;
    }

    /**
     * 添加connect事件，当客户端发起连接时调用
     */
    @OnConnect
    public void onConnect(SocketIOClient ioClient) {
        if (ioClient != null) {
            String type = ioClient.getHandshakeData().getSingleUrlParam("type");
            String userName = ioClient.getHandshakeData().getSingleUrlParam("name");
            String controlRoomId = ioClient.getHandshakeData().getSingleUrlParam(CONTROL_ROOM_ID);
            log.info("type=" + type + ",name=" + userName + ",controlRoomId=" + controlRoomId);
            clients.put(userName, ioClient);
            controlRoomIds.add(controlRoomId);
            if (StringUtils.isNotBlank(controlRoomId)) {
                String clientId = ioClient.getSessionId().toString();
                ioClient.set(CLIENT_ID, clientId);
                ioClient.set(CONTROL_ROOM_ID, controlRoomId);
                ioClient.joinRoom(controlRoomId);
                ioClient.joinRoom(ALL);
            }
        } else {
            log.error("客户端为空");
        }
    }

    /**
     * 添加onDisconnect事件，客户端断开连接时调用，刷新客户端信息
     */
    @OnDisconnect
    public void onDisconnect(SocketIOClient ioClient) {
        String type = ioClient.get("type");
        String clientId = ioClient.getSessionId().toString();
        String controlRoomId = ioClient.get(CONTROL_ROOM_ID);
        if (StringUtils.isNotBlank(controlRoomId)) {
            ioClient.leaveRoom(controlRoomId);
            log.info(controlRoomId + "断开连接");
            ioClient.leaveRoom(ALL);
        }
        ioClient.disconnect();
    }

    /**
     * 消息接收入口
     */
    @OnEvent(value = "message")
    public void onEvent(SocketIOClient ioClient, SocketMessage message) {
        log.info("接收message:{}", JSON.toJSONString(message));
        String type = ioClient.get("type");
        //推送到指定客户端(目标客户端)
        List<String> targetClients = ((SocketMessage) (message)).getTargetClientIds();
        targetClients.forEach(targetClient -> {
            pushByServer(targetClient, message);
        });

        //pushAll(message);
    }

    public void pushAll(SocketMessage message) {
        server.getBroadcastOperations().sendEvent("message", message);
    }

    /**
     * 通过服务器推送
     * @param controlRoomId
     * @param message
     */

    public void pushByServer(String controlRoomId, SocketMessage message) {
        controlRoomIds.stream().forEach(e -> {
            if (e.equals(controlRoomId)) {
                server.getRoomOperations(controlRoomId).sendEvent("message", message);
            }
        });

    }

    /**
     * 通过客户端推送
     *
     * @param userName
     * @param message
     */
    public void pushByClient(String userName, SocketMessage message) {
        clients.forEach((k, v) -> {
            if (userName.equals(k)) {
                v.sendEvent("message", message);
            }
        });
    }


    public Collection<SocketIOClient> getAllClient() {
        return server.getAllClients();
    }

}
