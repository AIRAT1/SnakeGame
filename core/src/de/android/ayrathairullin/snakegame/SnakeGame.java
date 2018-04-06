package de.android.ayrathairullin.snakegame;

import com.badlogic.gdx.Game;

public class SnakeGame extends Game {
	@Override
	public void create() {
		setScreen(new GameScreen());
	}
}
