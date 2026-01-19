package org.ruoyi.dingtalk.vo;

import com.alibaba.fastjson2.JSONObject;
import org.ruoyi.dingtalk.util.MessageType;

/**
 * 钉钉语音消息
 *
 * @author sunjianlei
 */
public class VoiceMessage extends SuperMessage {

    private JSONObject voice = new JSONObject();

    /**
     * 钉钉语音消息
     *
     * @param media_id 媒体文件ID。可以通过上传媒体文件接口获取。
     * @param duration 音频时长，正整数，小于60
     */
    public VoiceMessage(String media_id, int duration) {
        super(MessageType.VOICE);
        this.voice.put("media_id", media_id);
        this.voice.put("duration", duration);
    }

    public JSONObject getVoice() {
        return this.voice;
    }

}
