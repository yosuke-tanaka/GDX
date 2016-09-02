package jp.techacademy.yousuke.tanaka.jumpactiongame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class JumpActionGame extends Game {
	// publicにして外からアクセスできるようにする
	public SpriteBatch batch;

	@Override
	public void create () {
		batch = new SpriteBatch();

		// GameScreenを表示する
		setScreen(new GameScreen(this));
	}
}