package de.android.ayrathairullin.snakegame;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class GameScreen extends ScreenAdapter {
    private static final float MOVE_TIME = 1f;
    private static final int SNAKE_MOVEMENT = 32;

    private enum SnakeDirection {
        LEFT, RIGHT, UP, DOWN
    }

    private SnakeDirection snakeDirection = SnakeDirection.RIGHT;

    private SpriteBatch batch;
    private Texture snakeHead, apple, snakeBody;
    private float timer = MOVE_TIME;
    private int snakeX = 0, snakeY = 0, appleX, appleY, snakeXBeforeUpdate = 0, snakeYBeforeUpdate = 0;
    private boolean appleAvailable = false;
    private Array<BodyPart> bodyParts = new Array<BodyPart>();

    @Override
    public void show() {
        snakeHead = new Texture(Gdx.files.internal("snakehead.png"));
        apple = new Texture(Gdx.files.internal("apple.png"));
        snakeBody = new Texture(Gdx.files.internal("snakebody.png"));
        batch = new SpriteBatch();
    }

    @Override
    public void render(float delta) {
        queryInput();

        timer -= delta;
        if (timer <= 0) {
            timer = MOVE_TIME;
            moveSnake();
            checkForOutOfBounds();
            updateBodyPartsPosition();
            checkAppleCollision();
        }

        checkAndPlaceApple();
        clearScreen();
        draw();
    }

    private void draw() {
        batch.begin();
        batch.draw(snakeHead, snakeX, snakeY);
        for (BodyPart bodyPart : bodyParts) {
            bodyPart.draw(batch);
        }
        if (appleAvailable) batch.draw(apple, appleX, appleY);
        batch.end();
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void dispose() {
        snakeHead.dispose();
        apple.dispose();
    }

    private void checkForOutOfBounds() {
        if (snakeX >= Gdx.graphics.getWidth()) snakeX = 0;
        if (snakeX < 0) snakeX = Gdx.graphics.getWidth() - SNAKE_MOVEMENT;
        if (snakeY >= Gdx.graphics.getHeight()) snakeY = 0;
        if (snakeY < 0) snakeY = Gdx.graphics.getHeight() - SNAKE_MOVEMENT;
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

    private void queryInput() {
        if (Gdx.input.isKeyPressed(Keys.LEFT)) snakeDirection = SnakeDirection.LEFT;
        if (Gdx.input.isKeyPressed(Keys.RIGHT)) snakeDirection = SnakeDirection.RIGHT;
        if (Gdx.input.isKeyPressed(Keys.UP)) snakeDirection = SnakeDirection.UP;
        if (Gdx.input.isKeyPressed(Keys.DOWN)) snakeDirection = SnakeDirection.DOWN;
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
        if (snakeX == appleX && snakeY == appleY) {
            BodyPart bodyPart = new BodyPart(snakeBody);
            bodyPart.updateBodyPosition(snakeX, snakeY);
            bodyParts.insert(0, bodyPart);
            appleAvailable = false;
        }
    }

    private void updateBodyPartsPosition() {
        if (bodyParts.size > 0) {
            BodyPart bodyPart = bodyParts.removeIndex(0);
            bodyPart.updateBodyPosition(snakeXBeforeUpdate, snakeYBeforeUpdate);
            bodyParts.add(bodyPart);
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
