package com.benqio.dmm.scoketio.message;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Socket消息体
 */
@Getter
@Setter
@Component
public class SocketMessage {

    /**
     * 类型
     */
    private String type;

    /**
     * 客户端ID集合
     */
    @NotNull
    private List<String> targetClientIds;

    /**
     * 消息体
     */
    private JSONObject data;
}