package com.avos.avoscloud.upload;

import com.avos.avoscloud.AVErrorUtils;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVPowerfulUtils;
import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.GenericObjectCallback;
import com.avos.avoscloud.PaasClient;
import com.avos.avoscloud.ProgressCallback;
import com.avos.avoscloud.SaveCallback;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UrlDirectlyUploader extends HttpClientUploader {
  private UploadCallback callback = null;
  public UrlDirectlyUploader(AVFile avFile, SaveCallback saveCallback,
                                ProgressCallback progressCallback, UploadCallback uploadCallback) {
    super(avFile, saveCallback, progressCallback);
    this.callback = uploadCallback;
  }

  @Override
  public AVException doWork() {

    final AVException[] exceptionSaveFile = new AVException[1];
    String url = AVPowerfulUtils.getEndpoint(avFile, true);
    String params = getFileRequestParameters();
    System.out.println("try to post url=" + url + ", params=" + params);
    PaasClient.storageInstance().postObject(url, params, true,
        new GenericObjectCallback() {
          @Override
          public void onSuccess(String content, AVException e) {
            System.out.println("request successful result=" + content + ", exception=" + e);
            if (e == null) {
              try {
                JSONObject jsonObject = new JSONObject(content);
                if (null != callback) {
                  callback.finishedWithResults(jsonObject.getString("objectId"), avFile.getUrl());
                }
//                avFile.handleUploadedResponse(jsonObject.getString("objectId"),
//                    jsonObject.getString("objectId"), avFile.getUrl());
                publishProgress(100);
              } catch (Exception ex) {
                exceptionSaveFile[0] = new AVException(ex);
              }
            } else {
              exceptionSaveFile[0] = AVErrorUtils.createException(e, content);
            }
          }

          @Override
          public void onFailure(Throwable error, String content) {
            System.out.println("request failure result=" + content + ", exception=" + error.getMessage());
            exceptionSaveFile[0] = AVErrorUtils.createException(error, content);
          }

        });

    return exceptionSaveFile[0];
  }

  private String getFileRequestParameters() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("name", avFile.getName());
    parameters.put("mime_type", avFile.mimeType());
    parameters.put("metaData", avFile.getMetaData());
    parameters.put("__type", AVFile.className());
    parameters.put("url", avFile.getUrl());
    if (avFile.getACL() != null) {
      parameters.putAll(AVUtils.getParsedMap(avFile.getACL().getACLMap()));
    }
    return AVUtils.restfulServerData(parameters);
  }

}