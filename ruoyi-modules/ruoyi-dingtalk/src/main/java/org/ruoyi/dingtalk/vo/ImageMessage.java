package org.ruoyi.dingtalk.vo;

import com.alibaba.fastjson2.JSONObject;
import org.ruoyi.dingtalk.util.MessageType;

/**
 * 钉钉图片消息
 *
 * @author sunjianlei
 */
public class ImageMessage extends SuperMessage {

    private JSONObject image = new JSONObject();

    /**
     * 钉钉图片消息
     *
     * @param media_id 媒体文件 media_id。可以通过上传媒体文件接口获取。建议宽600像素 x 400像素，宽高比3 : 2。
     */
    public ImageMessage(String media_id) {
        super(MessageType.IMAGE);
        this.image.put("media_id", media_id);
    }

    public JSONObject getImage() {
        return this.image;
    }

}
