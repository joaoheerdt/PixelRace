package io.github.pixelrace;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.pixelrace.map.GameMap;
import io.github.pixelrace.vehicles.Vehicle;
import io.github.pixelrace.vehicles.VehicleLoader;

import java.util.List;
public class GameMain extends ApplicationAdapter implements InputProcessor {

    private static final int VIRTUAL_WIDTH = 1280;
    private static final int VIRTUAL_HEIGHT = 720;

    private Vehicle activeVehicle;
    private List<Vehicle> garageCars;
    private int currentCarIndex = 0;

    private GameMap activeMap;
    private boolean isAccelerating = false, isBraking = false;

    private enum GameStage {MENU, GARAGE, PLAYING, PAUSED, CONFIG}
    private GameStage currentState = GameStage.MENU;

    private Music menuMusic;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private GlyphLayout layout;
    private Viewport viewport;
    private OrthographicCamera camera;

    // Imagens
    private Texture menuBackground;
    private Texture garageBackground;
    private Texture menuConfigBackground;
    private Texture[] buttonImages;

    // Coordenadas base dos botões do menu (convenção top-down, igual ao original)
    private Rectangle[] buttons = {
            new Rectangle(490, 320, 300, 60), // 0: JOGAR
            new Rectangle(50, 600, 60, 60),   // 1: CONFIGURAÇÕES
            new Rectangle(515, 420, 250, 50), // 2: GARAGEM
            new Rectangle(1170, 600, 60, 60)  // 3: SAIR
    };

    private final Rectangle btnContinuar = new Rectangle(515, 230, 250, 45);
    private final Rectangle btnIrGaragem = new Rectangle(515, 300, 250, 45);
    private final Rectangle btnMenuPrincipal = new Rectangle(515, 370, 250, 45);

    private final Rectangle btnMusicaMenos = new Rectangle(480, 480, 45, 40);
    private final Rectangle btnMusicaMais = new Rectangle(755, 480, 45, 40);
    private final Rectangle btnEfeitosMenos = new Rectangle(480, 540, 45, 40);
    private final Rectangle btnEfeitosMais = new Rectangle(755, 540, 45, 40);

    // Botões da Garagem
    private final Rectangle btnCarroAnterior = new Rectangle(200, 400, 60, 60);
    private final Rectangle btnProximoCarro = new Rectangle(1020, 400, 60, 60);
    private final Rectangle btnSelecionarCarro = new Rectangle(520, 620, 240, 50);

    private int botaoPressionado = -1;
    private int volumeMusica = 80;
    private int volumeEfeitos = 80;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        layout = new GlyphLayout();

        camera = new OrthographicCamera();
        viewport = new ExtendViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);

        menuBackground = new Texture(Gdx.files.internal("menu/menu_background.png"));
        garageBackground = new Texture(Gdx.files.internal("menu/garage_background.png"));
        menuConfigBackground = new Texture(Gdx.files.internal("menu/fundo_menu_config.png"));
        buttonImages = new Texture[]{
                new Texture(Gdx.files.internal("menu/play_button.png")),
                new Texture(Gdx.files.internal("menu/config_button.png")),
                new Texture(Gdx.files.internal("menu/garage_button.png")),
                new Texture(Gdx.files.internal("menu/exit_button.png"))
        };

        this.garageCars = VehicleLoader.loadAllMods();
        if (this.garageCars != null && !this.garageCars.isEmpty()) {
            this.activeVehicle = this.garageCars.get(currentCarIndex);
            this.activeVehicle.setX(480f);
            this.activeVehicle.setY(480f);
        }


        this.activeMap = new GameMap("map/default_map.png");

        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("menu/sound/top_gear.wav"));
        menuMusic.setLooping(true);
        menuMusic.play();
        applyMusicVolume();

        Gdx.input.setInputProcessor(this);
    }

    private void applyMusicVolume() {
        if (menuMusic != null) menuMusic.setVolume(volumeMusica / 100f);
    }

    private float toDrawY(float topY, float h) {
        return VIRTUAL_HEIGHT - topY - h;
    }

    private Vector2 getVirtualPointTopDown(int screenX, int screenY) {
        Vector2 v = new Vector2(screenX, screenY);
        viewport.unproject(v);
        v.y = VIRTUAL_HEIGHT - v.y;
        return v;
    }

    private float getVirtualWidth() {
        return viewport.getWorldWidth();
    }

    // ---------------------------------------------------------------
    // Input
    // ---------------------------------------------------------------

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        float vWidth = getVirtualWidth();

        buttons[0].x = (vWidth - buttons[0].width) / 2;
        buttons[2].x = (vWidth - buttons[2].width) / 2;
        buttons[3].x = vWidth - 110;
        btnProximoCarro.x = vWidth - 260;

        Vector2 p = getVirtualPointTopDown(screenX, screenY);

        if (currentState == GameStage.MENU) {
            for (int i = 0; i < buttons.length; i++) {
                if (buttons[i].contains(p)) {
                    botaoPressionado = i;
                }
            }
        } else if (currentState == GameStage.GARAGE) {
            if (btnProximoCarro.contains(p)) {
                currentCarIndex = (currentCarIndex + 1) % garageCars.size();
            } else if (btnCarroAnterior.contains(p)) {
                currentCarIndex = (currentCarIndex - 1 + garageCars.size()) % garageCars.size();
            } else if (btnSelecionarCarro.contains(p)) {
                activeVehicle = garageCars.get(currentCarIndex);
                activeVehicle.setX(480);
                activeVehicle.setY(480);
                currentState = GameStage.MENU;
            }
        } else if (currentState == GameStage.PAUSED || currentState == GameStage.CONFIG) {
            if (currentState == GameStage.PAUSED && btnContinuar.contains(p)) {
                currentState = GameStage.PLAYING;
            } else if (btnIrGaragem.contains(p)) {
                currentState = GameStage.GARAGE;
            } else if (btnMenuPrincipal.contains(p)) {
                currentState = GameStage.MENU;
            } else if (btnMusicaMenos.contains(p)) {
                volumeMusica = Math.max(0, volumeMusica - 10);
                applyMusicVolume();
            } else if (btnMusicaMais.contains(p)) {
                volumeMusica = Math.min(100, volumeMusica + 10);
                applyMusicVolume();
            } else if (btnEfeitosMenos.contains(p)) {
                volumeEfeitos = Math.max(0, volumeEfeitos - 10);
                if (activeVehicle != null) activeVehicle.setVolumeEfeitos(volumeEfeitos);
            } else if (btnEfeitosMais.contains(p)) {
                volumeEfeitos = Math.min(100, volumeEfeitos + 10);
                if (activeVehicle != null) activeVehicle.setVolumeEfeitos(volumeEfeitos);
            }
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        Vector2 p = getVirtualPointTopDown(screenX, screenY);
        if (currentState == GameStage.MENU && botaoPressionado != -1 && buttons[botaoPressionado].contains(p)) {
            executarAcao(botaoPressionado);
        }
        botaoPressionado = -1;
        return true;
    }

    private void executarAcao(int index) {
        switch (index) {
            case 0:
                if (activeVehicle != null) {
                    activeVehicle.setX(100);
                    activeVehicle.setY(330);
                }
                currentState = GameStage.PLAYING;
                break;
            case 1:
                currentState = GameStage.CONFIG;
                break;
            case 2:
                currentState = GameStage.GARAGE;
                break;
            case 3:
                Gdx.app.exit();
                break;
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Keys.ESCAPE) {
            if (currentState == GameStage.PLAYING) {
                currentState = GameStage.PAUSED;
                isAccelerating = false;
                isBraking = false;
                if (activeVehicle != null) activeVehicle.stopEngineSounds();
            } else if (currentState == GameStage.PAUSED) {
                currentState = GameStage.PLAYING;
            } else if (currentState == GameStage.GARAGE || currentState == GameStage.CONFIG) {
                currentState = GameStage.MENU;
            }
        }

        if (currentState == GameStage.PLAYING && activeVehicle != null) {
            if (keycode == Keys.D) isAccelerating = true;
            if (keycode == Keys.A || keycode == Keys.S) isBraking = true;
            if (keycode >= Keys.NUM_0 && keycode <= Keys.NUM_5) activeVehicle.changeGear(keycode - Keys.NUM_0);
            if (keycode == Keys.ENTER) activeVehicle.toggleEngine();
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Keys.D) isAccelerating = false;
        if (keycode == Keys.A || keycode == Keys.S) isBraking = false;
        return true;
    }

    @Override
    public boolean keyTyped(char character) { return false; }
    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override
    public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override
    public boolean scrolled(float amountX, float amountY) { return false; }

    // ---------------------------------------------------------------
    // Render
    // ---------------------------------------------------------------

    @Override
    public void render() {
        if (currentState == GameStage.PLAYING && activeVehicle != null) {
            activeVehicle.updatePhysics(isAccelerating, isBraking);
            activeMap.update(activeVehicle.getCurrentSpeed());
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        float virtualWidthAtual = getVirtualWidth();

        if (currentState == GameStage.PLAYING || currentState == GameStage.PAUSED) {
            batch.begin();
            if (activeMap != null) activeMap.draw(batch, virtualWidthAtual, VIRTUAL_HEIGHT);
            if (activeVehicle != null) activeVehicle.draw(batch, VIRTUAL_HEIGHT);

            if (activeVehicle != null) {
                int currentGear = activeVehicle.getCurrentGear();
                double visualSpeed = activeVehicle.getCurrentSpeed() * 1.65;
                font.getData().setScale(1.3f);
                font.setColor(Color.WHITE);
                font.draw(batch, "Gear: " + (currentGear == 0 ? "N" : currentGear), 20, VIRTUAL_HEIGHT - 30);
                font.draw(batch, String.format("RPM: %.0f", activeVehicle.getCurrentRpm()), 20, VIRTUAL_HEIGHT - 60);
                font.draw(batch, String.format("Speed: %.0f km/h", visualSpeed), 20, VIRTUAL_HEIGHT - 90);
            }
            batch.end();

            if (currentState == GameStage.PAUSED) desenharMenuPause(virtualWidthAtual);

        } else if (currentState == GameStage.MENU) {
            batch.begin();
            batch.draw(menuBackground, 0, 0, virtualWidthAtual, VIRTUAL_HEIGHT);

            if (activeVehicle != null) {
                activeVehicle.setX(virtualWidthAtual / 2 - activeVehicle.getWidth() / 2f);
                activeVehicle.setY(470);
                activeVehicle.draw(batch, VIRTUAL_HEIGHT);
            }

            buttons[0].x = (virtualWidthAtual - buttons[0].width) / 2;
            buttons[2].x = (virtualWidthAtual - buttons[2].width) / 2;
            buttons[3].x = virtualWidthAtual - 110;

            for (int i = 0; i < buttonImages.length; i++) {
                float offset = (botaoPressionado == i) ? 4 : 0;
                batch.draw(buttonImages[i], buttons[i].x, toDrawY(buttons[i].y + offset, buttons[i].height),
                        buttons[i].width, buttons[i].height);
            }
            batch.end();

        } else if (currentState == GameStage.GARAGE) {
            batch.begin();
            batch.draw(garageBackground, 0, 0, virtualWidthAtual, VIRTUAL_HEIGHT);

            btnProximoCarro.x = virtualWidthAtual - 260;
            btnSelecionarCarro.x = virtualWidthAtual / 2 - btnSelecionarCarro.width / 2;

            if (garageCars != null && !garageCars.isEmpty()) {
                Vehicle previewVehicle = garageCars.get(currentCarIndex);
                previewVehicle.setX(virtualWidthAtual / 2 - previewVehicle.getWidth() / 2f);
                previewVehicle.setY(340);
                previewVehicle.draw(batch, VIRTUAL_HEIGHT);
            }
            batch.end();

            if (garageCars != null && !garageCars.isEmpty()) {
                String nomeCarro = garageCars.get(currentCarIndex).name.toUpperCase();
                font.getData().setScale(1.6f);
                layout.setText(font, nomeCarro);

                int paddingX = 40, paddingY = 15;
                float boxWidth = layout.width + (paddingX * 2);
                float boxHeight = layout.height + (paddingY * 2);
                float boxX = (virtualWidthAtual - boxWidth) / 2;
                float boxY = 140;

                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(15 / 255f, 18 / 255f, 28 / 255f, 220 / 255f);
                shapeRenderer.rect(boxX, toDrawY(boxY, boxHeight), boxWidth, boxHeight);
                shapeRenderer.end();

                Gdx.gl.glLineWidth(3);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(1f, 200 / 255f, 0f, 1f);
                shapeRenderer.rect(boxX, toDrawY(boxY, boxHeight), boxWidth, boxHeight);
                shapeRenderer.end();

                batch.begin();
                font.setColor(Color.WHITE);
                font.draw(batch, nomeCarro, boxX + paddingX, toDrawY(boxY, boxHeight) + boxHeight - paddingY);
                batch.end();
            }

            desenharBotaoMenu(btnCarroAnterior, "<", new Color(52 / 255f, 152 / 255f, 219 / 255f, 1f));
            desenharBotaoMenu(btnProximoCarro, ">", new Color(52 / 255f, 152 / 255f, 219 / 255f, 1f));
            desenharBotaoMenu(btnSelecionarCarro, "SELECIONAR", new Color(46 / 255f, 204 / 255f, 113 / 255f, 1f));

        } else if (currentState == GameStage.CONFIG) {
            desenharMenuConfig(virtualWidthAtual);
        }
    }

    private void desenharMenuPause(float virtualWidth) {
        batch.begin();
        batch.draw(menuConfigBackground, 0, 0, virtualWidth, VIRTUAL_HEIGHT);
        batch.end();

        float menuW = 450, menuH = 500;
        float menuX = (virtualWidth - menuW) / 2;
        float menuY = 120;

        desenharPainel(menuX, menuY, menuW, menuH);

        font.getData().setScale(1.9f);
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "JOGO PAUSADO", menuX + 115, toDrawY(menuY, 0) - 40);
        batch.end();

        btnContinuar.x = menuX + 100;
        btnIrGaragem.x = menuX + 100;
        btnMenuPrincipal.x = menuX + 100;

        desenharBotaoMenu(btnContinuar, "CONTINUAR", new Color(46 / 255f, 204 / 255f, 113 / 255f, 1f));
        desenharBotaoMenu(btnIrGaragem, "GARAGEM", new Color(52 / 255f, 152 / 255f, 219 / 255f, 1f));
        desenharBotaoMenu(btnMenuPrincipal, "MENU PRINCIPAL", new Color(231 / 255f, 76 / 255f, 60 / 255f, 1f));

        desenharControlesDeVolume(menuX);
    }

    private void desenharMenuConfig(float virtualWidth) {
        batch.begin();
        batch.draw(menuConfigBackground, 0, 0, virtualWidth, VIRTUAL_HEIGHT);
        batch.end();

        float menuW = 450, menuH = 500;
        float menuX = (virtualWidth - menuW) / 2;
        float menuY = 120;

        desenharPainel(menuX, menuY, menuW, menuH);

        font.getData().setScale(1.9f);
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "CONFIGURAÇÃO", menuX + 115, toDrawY(menuY, 0) - 40);
        batch.end();

        btnIrGaragem.x = menuX + 100;
        btnMenuPrincipal.x = menuX + 100;

        desenharBotaoMenu(btnIrGaragem, "GARAGEM", new Color(52 / 255f, 152 / 255f, 219 / 255f, 1f));
        desenharBotaoMenu(btnMenuPrincipal, "MENU PRINCIPAL", new Color(231 / 255f, 76 / 255f, 60 / 255f, 1f));

        desenharControlesDeVolume(menuX);
    }

    private void desenharPainel(float menuX, float menuY, float menuW, float menuH) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(40 / 255f, 42 / 255f, 54 / 255f, 1f);
        shapeRenderer.rect(menuX, toDrawY(menuY, menuH), menuW, menuH);
        shapeRenderer.end();

        Gdx.gl.glLineWidth(3);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 200 / 255f, 0f, 1f);
        shapeRenderer.rect(menuX, toDrawY(menuY, menuH), menuW, menuH);
        shapeRenderer.end();
    }

    private void desenharControlesDeVolume(float menuX) {
        btnMusicaMenos.x = menuX + 65;
        btnMusicaMais.x = menuX + 340;
        btnEfeitosMenos.x = menuX + 65;
        btnEfeitosMais.x = menuX + 340;

        font.getData().setScale(1.15f);
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "MÚSICA: " + volumeMusica + "%", menuX + 130, VIRTUAL_HEIGHT - 505);
        font.draw(batch, "EFEITOS: " + volumeEfeitos + "%", menuX + 130, VIRTUAL_HEIGHT - 565);
        batch.end();

        desenharBotaoMenu(btnMusicaMenos, "-", new Color(0.39f, 0.39f, 0.39f, 1f));
        desenharBotaoMenu(btnMusicaMais, "+", new Color(0.39f, 0.39f, 0.39f, 1f));
        desenharBotaoMenu(btnEfeitosMenos, "-", new Color(0.39f, 0.39f, 0.39f, 1f));
        desenharBotaoMenu(btnEfeitosMais, "+", new Color(0.39f, 0.39f, 0.39f, 1f));
    }

    private void desenharBotaoMenu(Rectangle rect, String texto, Color cor) {
        float drawY = toDrawY(rect.y, rect.height);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(cor);
        shapeRenderer.rect(rect.x, drawY, rect.width, rect.height);
        shapeRenderer.end();

        Gdx.gl.glLineWidth(1);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(rect.x, drawY, rect.width, rect.height);
        shapeRenderer.end();

        font.getData().setScale(1.15f);
        layout.setText(font, texto);
        float textX = rect.x + (rect.width - layout.width) / 2;
        float textY = drawY + (rect.height + layout.height) / 2;

        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, texto, textX, textY);
        batch.end();
    }

    // ---------------------------------------------------------------

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        menuBackground.dispose();
        garageBackground.dispose();
        menuConfigBackground.dispose();
        for (Texture t : buttonImages) t.dispose();
        if (activeMap != null) activeMap.dispose();
        if (garageCars != null) for (Vehicle v : garageCars) v.dispose();
        if (menuMusic != null) menuMusic.dispose();
    }
}
