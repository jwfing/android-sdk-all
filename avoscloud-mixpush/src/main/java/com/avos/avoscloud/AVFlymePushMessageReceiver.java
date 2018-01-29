package com.avos.avoscloud;

import android.content.Context;
import android.content.Intent;

import com.avos.avoscloud.utils.StringUtils;
import com.meizu.cloud.pushsdk.MzPushMessageReceiver;
import com.meizu.cloud.pushsdk.handler.MzPushMessage;
import com.meizu.cloud.pushsdk.notification.PushNotificationBuilder;
import com.meizu.cloud.pushsdk.platform.message.PushSwitchStatus;
import com.meizu.cloud.pushsdk.platform.message.RegisterStatus;
import com.meizu.cloud.pushsdk.platform.message.SubAliasStatus;
import com.meizu.cloud.pushsdk.platform.message.SubTagsStatus;
import com.meizu.cloud.pushsdk.platform.message.UnRegisterStatus;

/**
 * Created by wli on 2017/2/14.
 */

public class AVFlymePushMessageReceiver extends MzPushMessageReceiver {

  private final String FLYME_VERDOR = "mz";

  private void updateAVInstallation(String flymePushId) {
    if (!AVUtils.isBlankString(flymePushId)) {
      AVInstallation installation = AVInstallation.getCurrentInstallation();

      if (!FLYME_VERDOR.equals(installation.getString(AVInstallation.VENDOR))) {
        installation.put(AVInstallation.VENDOR, FLYME_VERDOR);
      }
      if (!flymePushId.equals(installation.getString(AVInstallation.REGISTRATION_ID))) {
        installation.put(AVInstallation.REGISTRATION_ID, flymePushId);
      }

      String localProfile = installation.getString(AVMixPushManager.MIXPUSH_PRIFILE);
      localProfile = (null != localProfile ? localProfile : "");
      if (!localProfile.equals(AVMixPushManager.flymeDevicePrifile)) {
        installation.put(AVMixPushManager.MIXPUSH_PRIFILE, AVMixPushManager.flymeDevicePrifile);
      }

      installation.saveInBackground(new SaveCallback() {
        @Override
        public void done(AVException e) {
          if (null != e) {
            LogUtil.avlog.d("update installation error!");
          } else {
            LogUtil.avlog.d("flyme push registration successful!");
          }
        }
      });
    }
  }


  /**
   * 处理透传消息
   *
   */

  @Override
  public void onMessage(Context context, String s) {
    if (null == context || null == s) {
      return;
    }
    if (AVOSCloud.isDebugLogEnabled()) {
      LogUtil.avlog.d("throughMessage coming, message=" + s);
    }
    AVNotificationManager.getInstance().processMixPushMessage(s);
  }

  @Override
  public void onMessage(Context var1, String message, String platformExtra) {
    // onMessage(Context context, String s) 实现一个即可
  }

  @Override
  public void onMessage(Context context, Intent intent) {
    // flyme3.0平台支持透传消息,只有本方法才能处理flyme3的透传消息,具体相见flyme3获取消息的方法
  }

  @Override
  public void onPushStatus(Context context, PushSwitchStatus pushSwitchStatus) {
    //检查通知栏和透传消息开关状态回调
    if (null == context || null == pushSwitchStatus) {
      return;
    }
    if (AVOSCloud.isDebugLogEnabled()) {
      LogUtil.avlog.d("switchNotificationMessage=" + pushSwitchStatus.isSwitchNotificationMessage()
          + ", switchThroughMessage=" + pushSwitchStatus.isSwitchThroughMessage() + ", pushId=" + pushSwitchStatus.getPushId());
    }
    String pushId = pushSwitchStatus.getPushId();
    if (!StringUtils.isBlankString(pushId)
        && (pushSwitchStatus.isSwitchNotificationMessage() || pushSwitchStatus.isSwitchThroughMessage())) {
      updateAVInstallation(pushId);
    }
  }

  /**
   * 处理设备注册事件
   *
   */

  @Override
  public void onRegisterStatus(Context context, RegisterStatus registerStatus) {
    //调用新版订阅PushManager.register(context,appId,appKey)回调
    if (null == context || null == registerStatus) {
      return;
    }
    if (AVOSCloud.isDebugLogEnabled()) {
      LogUtil.avlog.d("register successed, pushId=" + registerStatus.getPushId());
    }
    String pushId = registerStatus.getPushId();
    if (!AVUtils.isBlankContent(pushId)) {
      updateAVInstallation(pushId);
    }
  }

  @Override
  public void onUnRegisterStatus(Context context, UnRegisterStatus unRegisterStatus) {
    //新版反订阅回调
    if (null == context || null == unRegisterStatus) {
      return;
    }
    if (AVOSCloud.isDebugLogEnabled()) {
      LogUtil.avlog.d("unregister successed, message=" + unRegisterStatus.getMessage());
    }
  }

  @Override
  public void onSubTagsStatus(Context context, SubTagsStatus subTagsStatus) {
    //标签回调
  }

  @Override
  public void onSubAliasStatus(Context context, SubAliasStatus subAliasStatus) {
    //别名回调
  }

  @Override
  public void onUnRegister(Context var1, boolean var2) {}

  @Override
  public void onRegister(Context var1, String var2) {}

  /**
   * 处理通知栏消息
   *
   */

  @Override
  public void onUpdateNotificationBuilder(PushNotificationBuilder pushNotificationBuilder) {
    //重要,详情参考应用小图标自定设置
    if (AVMixPushManager.flymeMStatusBarIcon != 0) {
      pushNotificationBuilder.setmStatusbarIcon(AVMixPushManager.flymeMStatusBarIcon);
    }
  }

  @Override
  public void onNotificationArrived(Context context, MzPushMessage var2) {
    //通知栏消息到达回调，flyme6基于android6.0以上不再回调
  }

  @Override
  public void onNotificationClicked(Context context, MzPushMessage var2) {
    //通知栏消息点击回调
    if (null == context || null == var2) {
      return;
    }
    if (AVOSCloud.isDebugLogEnabled()) {
      LogUtil.avlog.d("notificationClicked, message=" + var2.getSelfDefineContentString());
    }
    String selfDefineContentString = var2.getSelfDefineContentString();
    AVNotificationManager.getInstance().processMixNotification(selfDefineContentString,
        AVConstants.AV_MIXPUSH_FLYME_NOTIFICATION_ACTION);
  }

  @Override
  public void onNotificationDeleted(Context context, MzPushMessage var2) {
    //通知栏消息删除回调；flyme6基于android6.0以上不再回调
  }

  @Override
  public void onNotifyMessageArrived(Context context, String message) {
  }
}
