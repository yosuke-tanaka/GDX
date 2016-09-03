package jp.techacademy.yousuke.tanaka.jumpactiongame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * ActivityRequestHandlerインタフェースのメソッドを呼び出す部分を実装します。
 * まず、ActivityRequestHandlerインタフェースを実装したAndroidLauncherを受け取らなければなりません。
 * そのためJumpActionGameクラスのコンストラクタに引数を追加します。
 * この時の引数のクラスはAndroidLauncherではなくActivityRequestHandlerとします。
 * なぜならばCore側ではAndroid側のクラスを知ることが出来ないからです。
 * 受け取ったActivityRequestHandlerはメンバ変数に保持しておきます。
 */
public class JumpActionGame extends Game {
	// publicにして外からアクセスできるようにする
	public SpriteBatch batch;

	public ActivityRequestHandler mRequestHandler;

	public JumpActionGame(ActivityRequestHandler requestHandler) {
		super();
		mRequestHandler = requestHandler;
	}

	@Override
	public void create () {
		batch = new SpriteBatch();

		// GameScreenを表示する
		setScreen(new GameScreen(this));
	}
}