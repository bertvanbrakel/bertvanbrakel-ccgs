package com.bertvanbrakel.ccgs;

import static org.apache.commons.lang.Validate.notNull;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.log4j.Logger;

public final class GameListenerHelper<T> implements GameListener<T> {
	
	private static final Logger LOG = Logger.getLogger(GameListenerHelper.class);

	private final Collection<GameListener<T>> listeners = new CopyOnWriteArraySet<GameListener<T>>();	
	
	public void register(GameListener<T> listener){
		notNull(listener,"null listener");
		listeners.add(listener);
	}
	
	public void deregister(GameListener<T> listener){
		listeners.remove(listener);
	}
	
	@Override
	public void onGameBegin(final Game<T> game) {
		invokeAll(new ListenerCallback<T>() {
			@Override
			public void invoke(GameListener<T> listener) {
				listener.onGameBegin(game);
			}
		});
	}

	@Override
	public void onMatchBegin(final Match<T> match) {
		invokeAll(new ListenerCallback<T>() {
			@Override
			public void invoke(GameListener<T> listener) {
				listener.onMatchBegin(match);
			}
		});
	}

	@Override
	public void onRoundBegin(final Round round) {
		invokeAll(new ListenerCallback<T>() {
			@Override
			public void invoke(GameListener<T> listener) {
				listener.onRoundBegin(round);
			}
		});
	}

	@Override
	public void onRoundEnd(final RoundResult<T> results) {
		invokeAll(new ListenerCallback<T>() {
			@Override
			public void invoke(GameListener<T> listener) {
				listener.onRoundEnd(results);
			}
		});
	}

	@Override
	public void onMatchEnd(final MatchResults<T> results) {
		invokeAll(new ListenerCallback<T>() {
			@Override
			public void invoke(GameListener<T> listener) {
				listener.onMatchEnd(results);
			}
		});
	}

	@Override
	public void onGameEnd(final Game<T> game) {
		invokeAll(new ListenerCallback<T>() {
			@Override
			public void invoke(GameListener<T> listener) {
				listener.onGameEnd(game);
			}
		});
	}
	
	private void invokeAll(ListenerCallback<T> callback) {
		for (GameListener<T> listener : listeners) {
			try {
				callback.invoke(listener);
			} catch (Exception e) {
				LOG.warn("error calling game listener " + listener, e);
				//further ignore, don't want other listeners to be affected
			}
		}
	}
	
	private static interface ListenerCallback<T> {
		void invoke(GameListener<T> listener);
	}

}
