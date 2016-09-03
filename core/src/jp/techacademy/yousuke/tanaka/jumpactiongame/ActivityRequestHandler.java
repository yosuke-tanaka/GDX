package jp.techacademy.yousuke.tanaka.jumpactiongame;

/**
 * 今回はCore側にActivityRequestHandlerインタフェースを作成し、Android側のAndroidLauncherで実装させます。
 * ActivityRequestHandlerは以下のようにshowAdsメソッドを実装させるものとなります
 */
public interface ActivityRequestHandler {
    public void showAds(boolean show);
}
