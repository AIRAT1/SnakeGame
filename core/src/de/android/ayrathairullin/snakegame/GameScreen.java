package de.android.ayrathairullin.snakegame;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen extends ScreenAdapter {
    private static final float MOVE_TIME = .5f;
    private static final int SNAKE_MOVEMENT = 32;
    private static final int GRID_CELL = 32;
    private static final String GAME_OVER_TEXT = "Game Over... Tap space to restart!";
    private static final int POINTS_PER_APPLE = 1;

    private enum SnakeDirection {
        LEFT, RIGHT, UP, DOWN
    }

    private enum State {
        PLAYING, GAME_OVER
    }

    private SnakeDirection snakeDirection = SnakeDirection.RIGHT;
    private State state = State.PLAYING;

    private SpriteBatch batch;
    private Texture snakeHead, apple, snakeBody;
    private float timer = MOVE_TIME;
    private int snakeX = 0, snakeY = 0, appleX, appleY, snakeXBeforeUpdate = 0, snakeYBeforeUpdate = 0;
    private boolean appleAvailable = false;
    private Array<BodyPart> bodyParts = new Array<BodyPart>();

    private ShapeRenderer shapeRenderer;
    private boolean directionSet = false;

    private BitmapFont bitmapFont;
    private GlyphLayout layout;
    private Viewport viewport;
    private int score = 0;

    @Override
    public void show() {
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        layout = new GlyphLayout();
        bitmapFont = new BitmapFont();
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        snakeHead = new Texture(Gdx.files.internal("snakehead.png"));
        apple = new Texture(Gdx.files.internal("apple.png"));
        snakeBody = new Texture(Gdx.files.internal("snakebody.png"));
    }

    @Override
    public void dispose() {
        bitmapFont.dispose();
        shapeRenderer.dispose();
        batch.dispose();
        snakeHead.dispose();
        apple.dispose();
        snakeBody.dispose();
    }

    @Override
    public void render(float delta) {
        switch (state) {
            case PLAYING:
                queryInput();
                updateSnacke(delta);
                checkAppleCollision();
                checkAndPlaceApple();
                break;
            case GAME_OVER:
                checkForRestart();
                break;
        }
        clearScreen();
//        drawGrid();
        draw();
    }

    private void queryInput() {
        if (Gdx.input.isKeyPressed(Keys.LEFT)) updateDirection(SnakeDirection.LEFT);
        if (Gdx.input.isKeyPressed(Keys.RIGHT)) updateDirection(SnakeDirection.RIGHT);
        if (Gdx.input.isKeyPressed(Keys.UP)) updateDirection(SnakeDirection.UP);
        if (Gdx.input.isKeyPressed(Keys.DOWN)) updateDirection(SnakeDirection.DOWN);
    }

    private void updateDirection(SnakeDirection newSnakeDirection) {
        if (!directionSet && snakeDirection != newSnakeDirection) {
            directionSet = true;
            switch (newSnakeDirection) {
                case LEFT:
                    updateIfNotOppositeDirection(newSnakeDirection, SnakeDirection.RIGHT);
                    break;
                case RIGHT:
                    updateIfNotOppositeDirection(newSnakeDirection, SnakeDirection.LEFT);
                    break;
                case UP:
                    updateIfNotOppositeDirection(newSnakeDirection, SnakeDirection.DOWN);
                    break;
                case DOWN:
                    updateIfNotOppositeDirection(newSnakeDirection, SnakeDirection.UP);
                    break;
            }
        }
    }

    private void updateIfNotOppositeDirection(SnakeDirection newSnakeDirection, SnakeDirection oppositeDirection) {
        if (snakeDirection != oppositeDirection || bodyParts.size == 0) snakeDirection = newSnakeDirection;
    }

    private void updateSnacke(float delta) {
            timer -= delta;
            if (timer <= 0) {
                timer = MOVE_TIME;
                moveSnake();
                checkForOutOfBounds();
                updateBodyPartsPosition();
                checkSnackBodyCollision();
                directionSet = false;
            }
    }

    private void moveSnake() {
        snakeXBeforeUpdate = snakeX;
        snakeYBeforeUpdate = snakeY;
        switch (snakeDirection) {
            case RIGHT:
                snakeX += SNAKE_MOVEMENT;
                break;
            case LEFT:
                snakeX -= SNAKE_MOVEMENT;
                break;
            case UP:
                snakeY += SNAKE_MOVEMENT;
                break;
            case DOWN:
                snakeY -= SNAKE_MOVEMENT;
                break;
        }
    }

    private void checkForOutOfBounds() {
        if (snakeX >= Gdx.graphics.getWidth()) snakeX = 0;
        if (snakeX < 0) snakeX = Gdx.graphics.getWidth() - SNAKE_MOVEMENT;
        if (snakeY >= Gdx.graphics.getHeight()) snakeY = 0;
        if (snakeY < 0) snakeY = Gdx.graphics.getHeight() - SNAKE_MOVEMENT;
    }

    private void updateBodyPartsPosition() {
        if (bodyParts.size > 0) {
            BodyPart bodyPart = bodyParts.removeIndex(0);
            bodyPart.updateBodyPosition(snakeXBeforeUpdate, snakeYBeforeUpdate);
            bodyParts.add(bodyPart);
        }
    }

    private void checkAndPlaceApple() {
        if (!appleAvailable) {
            do {
                appleX = MathUtils.random(Gdx.graphics.getWidth() / SNAKE_MOVEMENT - 1) * SNAKE_MOVEMENT;
                appleY = MathUtils.random(Gdx.graphics.getHeight() / SNAKE_MOVEMENT - 1) * SNAKE_MOVEMENT;
                appleAvailable = true;
            }while (appleX == snakeX && appleY == snakeY);
        }
    }

    private void checkAppleCollision() {
        if (appleAvailable && snakeX == appleX && snakeY == appleY) {
            BodyPart bodyPart = new BodyPart(snakeBody);
            bodyPart.updateBodyPosition(snakeX, snakeY);
            bodyParts.insert(0, bodyPart);
            addToScore();
            appleAvailable = false;
        }
    }

    private void checkSnackBodyCollision() {
        for (BodyPart bodyPart : bodyParts) {
            if (bodyParts.size > 3) {
                if (bodyPart.x == snakeX && bodyPart.y == snakeY) state = State.GAME_OVER;
            }
        }
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void drawGrid() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (int x = 0; x < Gdx.graphics.getWidth(); x+= GRID_CELL) {
            for (int y = 0; y < Gdx.graphics.getHeight(); y+= GRID_CELL) {
                shapeRenderer.rect(x, y, GRID_CELL, GRID_CELL);
            }
        }
        shapeRenderer.end();
    }

    private void draw() {
        batch.begin();
        batch.draw(snakeHead, snakeX, snakeY);
        for (BodyPart bodyPart : bodyParts) {
            bodyPart.draw(batch);
        }
        if (appleAvailable) batch.draw(apple, appleX, appleY);
        if (state == State.GAME_OVER) {
            layout.setText(bitmapFont, GAME_OVER_TEXT);
            bitmapFont.draw(batch, GAME_OVER_TEXT, (viewport.getWorldWidth() - layout.width) / 2,
                    (viewport.getWorldHeight() - layout.height) / 2);
        }
        drawScore();
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    private void checkForRestart() {
        if (Gdx.input.isKeyPressed(Keys.SPACE)) {
            doRestart();
        }
    }

    private void doRestart() {
        state = State.PLAYING;
        bodyParts.clear();
        snakeDirection = SnakeDirection.RIGHT;
        directionSet = false;
        timer = MOVE_TIME;
        snakeX = 0;
        snakeY = 0;
        snakeXBeforeUpdate = 0;
        snakeYBeforeUpdate = 0;
        appleAvailable = false;
        score = 0;
    }

    private void addToScore() {
        score += POINTS_PER_APPLE;
    }

    private void drawScore() {
        if (state == State.PLAYING) {
            bitmapFont.draw(batch, String.valueOf(score), Gdx.graphics.getWidth() -
                    Gdx.graphics.getWidth() / 15f, Gdx.graphics.getHeight() * .95f);
        }
    }

    private class BodyPart {
        private int x, y;
        private Texture texture;

        public BodyPart(Texture texture) {
            this.texture = texture;
        }

        public void updateBodyPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void draw(Batch batch) {
            if (!(x == snakeX && y == snakeY)) batch.draw(texture, x, y);
        }
    }
}
