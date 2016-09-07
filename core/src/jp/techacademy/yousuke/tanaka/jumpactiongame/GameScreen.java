package jp.techacademy.yousuke.tanaka.jumpactiongame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameScreen extends ScreenAdapter {
    // カメラのサイズを表す定数
    static final float CAMERA_WIDTH = 10;
    static final float CAMERA_HEIGHT = 15;

    static final float WORLD_WIDTH = 10;
    static final float WORLD_HEIGHT = 15 * 8; // xx画面分登れば終了

    // GUI用のカメラのサイズ
    static final float GUI_WIDTH = 320;
    static final float GUI_HEIGHT = 480;

    static final int GAME_STATE_READY = 0;
    static final int GAME_STATE_PLAYING = 1;
    static final int GAME_STATE_GAMEOVER = 2;

    // 重力
    static final float GRAVITY = -12;

    private JumpActionGame mGame;

    Sprite mBg;

    // カメラを表すOrthographicCameraクラスと、ビューポートのFitViewportクラスをメンバ変数として定義
    OrthographicCamera mCamera;
    OrthographicCamera mGuiCamera;

    FitViewport mViewPort;
    FitViewport mGuiViewPort;

    Random mRandom;
    List<Step> mSteps;
    List<Star> mStars;
    Ufo mUfo;
    Player mPlayer;
    List<Enemy> mEnemy;

    static final int NUM_ENEMY_KIND = 3;

    // 高さからプレイヤーが地面からどれだけ離れたかを保持
    float mHeightSoFar;
    int mGameState;

    // タッチされた座標を保持
    Vector3 mTouchPoint;

    BitmapFont mFont;
    int mScore;
    int mHighScore;

    Preferences mPrefs;




    /**
     * 各画面に相当するScreenはScreenAdapterクラスを継承します。
     * コンストラクタでは引数で受け取ったJumpActionGameクラスのオブジェクトをメンバ変数に保持します。
     *
     * その他重要なクラスはSpriteクラスとTextureクラスです。
     * Spriteクラスは名前の通りスプライトです。
     * スプライトとはコンピュータの処理の負荷を上げずに高速に画像を描画する仕組みです。
     * 「プレイヤーや地面などの画像を表示するためのもの」という認識で問題ありません。
     * Textureクラスはテクスチャを表すクラスで、スプライトに貼り付ける画像のことです。
     * Textureクラスは画像のファイル名を指定して生成します。
     * そして、SpriteクラスにTextureRegionクラスのオブジェクトとして生成したものを指定します。
     * TextureRegionクラスはテクスチャとして用意した画像の一部を切り取ってスプライトに貼り付けるためのものです。
     * なぜ、切り取る必要があるのでしょうか？
     * 指定したback.pngが実際にどのような画像かを実際にAndroid Studio上で開いて確認してみましょう。
     *
     * Textureクラスで指定する画像は縦横は2の累乗、すなわち1,2,4,8,16,32,64,128,256,512,1024のような数字である必要があるのです。
     * そのため、背景画像は 540 x 810 で作成したとしても画像ファイルとしては 1024 x 1024 である必要があり、
     * このように切り出すことになります。
     * 今回は1ファイルにつき1つの画像としていますが、例えばback.pngは右半分には多くのスペースがあります。
     * ここに他の画像、例えばプレイヤーの画像などを用意しておき、1つの画像ファイルを読み込んだTextureクラスのオブジェクトから、
     * 複数回別の位置の画像を切り出して別々のスプライトに指定するという使い方もあります。
     *
     * SpriteクラスのsetPositionメソッドで描画する位置を指定します。左下を基準として位置を指定します。
     *
     * -----
     *
     * @param game
     */
    public GameScreen(JumpActionGame game) {
        mGame = game;

        // 背景の準備
        Texture bgTexture = new Texture("back.png");
        // TextureRegionで切り出す時の原点は左上
        mBg = new Sprite( new TextureRegion(bgTexture, 0, 0, 540, 810));
        mBg.setSize(CAMERA_WIDTH, CAMERA_HEIGHT);
        mBg.setPosition(0, 0);

        // カメラ、ViewPortを生成、設定する
        // コンストラクタでこれらメンバ変数に初期化して代入します。
        // ポイントはカメラのサイズとビューポートのサイズをどちらもCAMERA_WIDTHとCAMERA_HEIGHTを使って同じにするということです。
        // どちらも同じにしているため縦横比が固定されます。縦横比が固定されるので物理ディスプレイの比率と異なる場合は上下または左右に隙間ができるということです。
        // 実行する際に複数の端末で試すか、様々な解像度のエミュレータを作成して試すとよく分かることでしょう。
        mCamera = new OrthographicCamera();
        mCamera.setToOrtho(false, CAMERA_WIDTH, CAMERA_HEIGHT);
        mViewPort = new FitViewport(CAMERA_WIDTH, CAMERA_HEIGHT, mCamera);

        // GUI用のカメラを設定する
        mGuiCamera = new OrthographicCamera();
        mGuiCamera.setToOrtho(false, GUI_WIDTH, GUI_HEIGHT);
        mGuiViewPort = new FitViewport(GUI_WIDTH, GUI_HEIGHT, mGuiCamera);

        // メンバ変数の初期化
        mRandom = new Random();
        mSteps = new ArrayList<Step>();
        mStars = new ArrayList<Star>();
        mEnemy = new ArrayList<Enemy>();
        mGameState = GAME_STATE_READY;

        mTouchPoint = new Vector3();


        mFont = new BitmapFont(Gdx.files.internal("font.fnt"), Gdx.files.internal("font.png"), false);
        mFont.getData().setScale(0.8f);
        mScore = 0;
        mHighScore = 0;

        // ハイスコアをPreferencesから取得する
        mPrefs = Gdx.app.getPreferences("jp.techacademy.taro.kirameki.jumpactiongame");
        mHighScore = mPrefs.getInteger("HIGHSCORE", 0);


        createStage();
    }

    /**
     *コンストラクタで準備したスプライトをrenderメソッド内で描画します。
     * ScreenAdapterを継承したクラスのrenderメソッドは基本的に1/60秒ごとに自動的に呼び出されます。
     * 性能の低い古いAndroid端末や処理が重たい場合にはもう少し間隔が伸びる場合もあります。
     *
     * Gdx.gl.glClearColor(0, 0, 0, 1);と Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);で画面を描画する準備を行います。
     * glClearColorメソッドは画面をクリアする時の色を赤、緑、青、透過で指定します。
     * そしてGdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);で実際にその色でクリア（塗りつぶし）を行います。
     *
     * もう１つ大事なルールはスプライトなどを描画する際はSpriteBatchクラスのbeginメソッドとendメソッドの間で行うというルールです。
     * ここではJumpActionGameクラスのメンバ変数で保持しているSpriteBatchクラスのオブジェクトを使います。
     * そしてSpriteクラスのdrawメソッドを呼び出すことで描画します。
     *
     * --------
     *
     *カメラのupdateメソッドを呼び出し、
     * SpriteBatchクラスのsetProjectionMatrixメソッドをOrthographicCameraクラスのcombinedプロパティを引数に与えて呼び出します。
     * これはカメラの座標をアップデート（計算）し、スプライトの表示に反映させるために必要な呼び出しです。
     * これらの呼び出しによって物理ディスプレイに依存しない表示を行うことができます。
     * カメラのupdateメソッドでは行列演算を行ってカメラの座標値の再計算を行ってくれるメソッドです。
     * そしてsetProjectionMatrixメソッドとcombinedメソッドでその座標をスプライトに反映しています。
     *
     *
     * @param delta
     */
    @Override
    public void render (float delta) {

        // 状態を更新する
        update(delta);

        // 描画する
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // カメラの中心を超えたらカメラを上に移動させる つまりキャラが画面の上半分には絶対に行かない
        if (mPlayer.getY() > mCamera.position.y) {
            mCamera.position.y = mPlayer.getY();
        }

        // カメラの座標をアップデート（計算）し、スプライトの表示に反映させる
        mCamera.update();
        mGame.batch.setProjectionMatrix(mCamera.combined);

        mGame.batch.begin();

        // 背景
        // 原点は左下
        mBg.setPosition(mCamera.position.x - CAMERA_WIDTH / 2, mCamera.position.y - CAMERA_HEIGHT / 2);
        mBg.draw(mGame.batch);

        // Step
        for (int i = 0; i < mSteps.size(); i++) {
            mSteps.get(i).draw(mGame.batch);
        }

        // Star
        for (int i = 0; i < mStars.size(); i++) {
            mStars.get(i).draw(mGame.batch);
        }

        // Enemy
        for (int i = 0; i < mEnemy.size(); i++) {
            mEnemy.get(i).draw(mGame.batch);
        }

        // UFO
        mUfo.draw(mGame.batch);

        //Player
        mPlayer.draw(mGame.batch);

        mGame.batch.end();




        // スコア表示
        mGuiCamera.update();
        mGame.batch.setProjectionMatrix(mGuiCamera.combined);
        mGame.batch.begin();
        mFont.draw(mGame.batch, "HighScore: " + mHighScore, 16, GUI_HEIGHT - 15);
        mFont.draw(mGame.batch, "Score: " + mScore, 16, GUI_HEIGHT - 35);
        mGame.batch.end();
    }

    /**
     * 最後にresizeメソッドをオーバーライドしてFitViewportクラスのupdateメソッドを呼び出します。
     * resizeメソッドは物理的な画面のサイズが変更されたときに呼び出されるメソッドです。
     * Androidではcreate直後やバックグランドから復帰したときに呼び出されます。
     *
     * @param width
     * @param height
     */
    @Override
    public void resize(int width, int height) {
        mViewPort.update(width, height);
        mGuiViewPort.update(width, height);
    }


    // ステージを作成する
    private void createStage() {

        // テクスチャの準備
        Texture stepTexture = new Texture("step.png");
        Texture starTexture = new Texture("star.png");
        Texture playerTexture = new Texture("uma.png");
        Texture ufoTexture = new Texture("ufo.png");
        Texture tmpTexture;
        Texture[] enemyTextures = new Texture[NUM_ENEMY_KIND];
        enemyTextures[0] = new Texture("enemy1.png");
        enemyTextures[1] = new Texture("enemy2.png");
        enemyTextures[2] = new Texture("enemy3.png");

        // StepとStarをゴールの高さまで配置していく
        float y = 0;

        float maxJumpHeight = Player.PLAYER_JUMP_VELOCITY * Player.PLAYER_JUMP_VELOCITY / (2 * -GRAVITY);
        while (y < WORLD_HEIGHT - 5) {
            int type = mRandom.nextFloat() > 0.8f ? Step.STEP_TYPE_MOVING : Step.STEP_TYPE_STATIC;
            float x = mRandom.nextFloat() * (WORLD_WIDTH - Step.STEP_WIDTH);

            // Step
            Step step = new Step(type, stepTexture, 0, 0, 144, 36);
            step.setPosition(x, y);
            mSteps.add(step);

            // Star
            if (mRandom.nextFloat() > 0.6f) {
                Star star = new Star(starTexture, 0, 0, 72, 72);
                star.setPosition(step.getX() + mRandom.nextFloat(), step.getY() + Star.STAR_HEIGHT + mRandom.nextFloat() * 3);
                mStars.add(star);
            }

            // Enemy
            else if (mRandom.nextFloat() > 0.7f) {
                // ランダムでグラフィックを変更
                tmpTexture = enemyTextures[Math.abs(mRandom.nextInt()) % 3];
                Enemy enemy = new Enemy(tmpTexture, 0, 0, 72, 72);

                float offset = (mRandom.nextFloat() > 1.0f) ?  (mRandom.nextFloat() * 2) : (mRandom.nextFloat() * (-2));
                enemy.setPosition(step.getX() + offset, step.getY() + Enemy.ENEMY_HEIGHT + mRandom.nextFloat() * 3);

                mEnemy.add(enemy);
            }

            y += (maxJumpHeight - 0.5f);
            y -= mRandom.nextFloat() * (maxJumpHeight / 3);
        }

        // Playerを配置
        mPlayer = new Player(playerTexture, 0, 0, 72, 72);
        mPlayer.setPosition(WORLD_WIDTH / 2 - mPlayer.getWidth() / 2, Step.STEP_HEIGHT);

        // ゴールのUFOを配置
        mUfo = new Ufo(ufoTexture, 0, 0, 120, 74);
        mUfo.setPosition(WORLD_WIDTH / 2 - Ufo.UFO_WIDTH / 2, y);
    }

    // それぞれのオブジェクトの状態をアップデートする
    private void update(float delta) {
        switch (mGameState) {
            case GAME_STATE_READY:
                updateReady();
                break;
            case GAME_STATE_PLAYING:
                updatePlaying(delta);
                break;
            case GAME_STATE_GAMEOVER:
                updateGameOver();
                break;
        }
    }
    private void updateReady() {
        if (Gdx.input.justTouched()) {
            mGameState = GAME_STATE_PLAYING;
        }
    }

    /**
     * ゲーム中の updatePlayingメソッドではタッチされていたら、その座標が画面の左側なのか右側なのかを判断します。タッチされた座標はGdx.input.getX()とGdx.input.getY()で取得することができます。それらの値をmTouchPointにsetメソッドで設定します。Vector3クラスはx,yだけでなくZ軸を保持するメンバ変数zも持っているためsetメソッドの第3引数には0を指定しています。そしてそのmTouchPointをOrthographicCameraクラスのunprojectメソッドに与えて呼び出すことでカメラを使った座標に変換します。画面のどこをタッチされたかの判断はRectangleクラスの左半分を表す矩形leftと右半分を表す矩形rightを定義し、containsメソッドにタッチされた座標を与えることでその領域をタッチしているのか判断します。左側をタッチされた時は加速度としてaccel = 5.0fと、右側を タッチされたときはaccel = -5.0fを設定します。

     そしてその加速度をPlayerクラスのupdateメソッドに与えて呼び出します。踏み台の状態も更新させるためStepクラスのupdateメソッドも呼び出します。また、プレイヤーの座標が0.5以下になった場合は踏み台に乗ったと同じ処理(hitStepメソッド)を行い、ジャンプさせます。これはゲーム開始時にジャンプさせるための処理です。

     updatePlayingメソッドの最後はプレイヤーがどれだけ地面から離れたかを、Mathクラスのmaxメソッドを呼び出して保持している距離か、今のプレイヤーの高さか大きい方を保持します。
     * @param delta
     */
    private void updatePlaying(float delta) {
        float accel = 0;
        if (Gdx.input.isTouched()) {

//            mViewPort.unproject(mTouchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));
//            Rectangle left = new Rectangle(0, 0, CAMERA_WIDTH / 2, CAMERA_HEIGHT);
//            Rectangle right = new Rectangle(CAMERA_WIDTH / 2, 0, CAMERA_WIDTH / 2, CAMERA_HEIGHT);

            // updatePlayingメソッドのタッチイベントを処理している箇所をGUI用のカメラに変更
            mGuiViewPort.unproject(mTouchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));
            Rectangle left = new Rectangle(0, 0, GUI_WIDTH / 2, GUI_HEIGHT);
            Rectangle right = new Rectangle(GUI_WIDTH / 2, 0, GUI_WIDTH / 2, GUI_HEIGHT);

            if (left.contains(mTouchPoint.x, mTouchPoint.y)) {
                accel = 5.0f;
            }
            if (right.contains(mTouchPoint.x, mTouchPoint.y)) {
                accel = -5.0f;
            }
        }

        // Step
        for (int i = 0; i < mSteps.size(); i++) {
            mSteps.get(i).update(delta);
        }

        // Enemy
        for (int i = 0; i < mEnemy.size(); i++) {
            mEnemy.get(i).update(delta);
        }

        // Player
        if (mPlayer.getY() <= 0.5f) {
            mPlayer.hitStep();
        }
        mPlayer.update(delta, accel);
        mHeightSoFar = Math.max(mPlayer.getY(), mHeightSoFar);

        // 当たり判定を行う
        checkCollision();

        // ゲームオーバーか判断する
        checkGameOver();
    }

    /**
     * GameScreenでゲーム終了後にタッチしたらResultScreenに遷移するようにします
     */
    private void updateGameOver() {
        if (Gdx.input.justTouched()) {
            mGame.setScreen(new ResultScreen(mGame, mScore));
        }
    }

    /**
     * checkGameOverはプレイヤーの地面との距離であるmHeightSoFarから、カメラの高さの半分を引いた値よりプレイヤーの位置が低くなったらゲームオーバーとします。
     * これは画面の下までプレイヤーが落ちたらゲームオーバーとすることを表します。
     */
    private void checkGameOver() {
        if (mHeightSoFar - CAMERA_HEIGHT / 2 > mPlayer.getY()) {
            mPlayer.kill();
            Gdx.app.log("JampActionGame", "GAMEOVER");
            mGameState = GAME_STATE_GAMEOVER;
        }
    }

    /**
     * 当たり判定を行うには当たり判定を行うオブジェクト（スプライト）の矩形同士が重なっているかを判断し、
     * 重なっていれば当たっていると判断します。
     * SpriteクラスのgetBoundingRectangleメソッドでスプライトの矩形を表すRectangleを取得します。
     * 各オブジェクトのクラスはGameObjectクラスを継承しており、
     * GameObjectクラスはSpriteクラスを継承しているのでgetBoundingRectangleメソッドを呼び出すことができます。
     * Rectangleクラスのoverlapsメソッドに当たり判定を行いたい相手のRectangleを指定します。
     * 戻り値がtrueであれば重なっている＝当たっていることになります。

     UFOと当たった場合はゲームクリアとなるので状態をGAME_STATE_GAMEOVERにしてメソッドを抜けます。

     星との当たり判定は相手となるStarクラスのmStateがStar.STAR_NONEの場合はすでに当たって獲得済みなので当たり判定を行いません。
     踏み台との当たり判定はプレイヤーが上昇中＝mPlayer.velocity.y > 0の時は行いません。

     踏み台との当たり判定はstep.mState == Step.STEP_STATE_VANISH以外のもので判定を行います。
     踏み台と当たった場合はmRandom.nextFloat() > 0.5fで判断して、つまり1/2の確率で踏み台を消します。
     ゲームバランスを調整する際はこの値も変更してみると良いでしょう。
     */
    private void checkCollision() {

        // UFO(ゴールとの当たり判定)
        if (mPlayer.getBoundingRectangle().overlaps(mUfo.getBoundingRectangle())) {
            Sound soundCrear = Gdx.audio.newSound(Gdx.files.internal("se/crear.mp3"));
            soundCrear.play(1.0f);
            soundCrear.dispose();

            mGameState = GAME_STATE_GAMEOVER;
            return;
        }

        // Enemyとの当たり判定
        for (int i = 0; i < mEnemy.size(); i++) {
            Enemy enemy = mEnemy.get(i);

            if (mPlayer.getBoundingRectangle().overlaps(enemy.getBoundingRectangle())) {
                // 敵に衝突した
                // 効果音を鳴らす
                Sound soundKill = Gdx.audio.newSound(Gdx.files.internal("se/kill.mp3"));
                soundKill.play(1.0f);
                soundKill.dispose();

                // プレーヤーを非表示化
                mPlayer.kill();

                //ゲームオーバー
                mGameState = GAME_STATE_GAMEOVER;
                return;
            }
        }

        // Starとの当たり判定
        for (int i = 0; i < mStars.size(); i++) {
            Star star = mStars.get(i);

            if (star.mState == Star.STAR_NONE) {
                continue;
            }

            if (mPlayer.getBoundingRectangle().overlaps(star.getBoundingRectangle())) {
                Sound soundStar = Gdx.audio.newSound(Gdx.files.internal("se/star.mp3"));
                soundStar.play(1.0f);
                soundStar.dispose();

                star.get();

                mScore++;
                if (mScore > mHighScore) {
                    mHighScore = mScore;

                    //ハイスコアをPreferenceに保存する
                    mPrefs.putInteger("HIGHSCORE", mHighScore);
                    mPrefs.flush();
                }
                break;
            }
        }

        // Stepとの当たり判定
        // 上昇中はStepとの当たり判定を確認しない
        if (mPlayer.velocity.y > 0) {
            // NoOperation
        }
        else {
            for (int i = 0; i < mSteps.size(); i++) {
                Step step = mSteps.get(i);

                if (step.mState == Step.STEP_STATE_VANISH) {
                    continue;
                }

                if (mPlayer.getY() > step.getY()) {
                    if (mPlayer.getBoundingRectangle().overlaps(step.getBoundingRectangle())) {
                        Sound soundJump = Gdx.audio.newSound(Gdx.files.internal("se/jump.mp3"));
                        soundJump.play(1.0f);
                        soundJump.dispose();

                        mPlayer.hitStep();
                        if (mRandom.nextFloat() > 0.5f) {
                            step.vanish();
                        }
                        break;
                    }
                }
            }
        }
    }

}