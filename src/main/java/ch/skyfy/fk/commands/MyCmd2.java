package ch.skyfy.fk.commands;

import ch.skyfy.fk.logic.FKGame;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public abstract class MyCmd2 {

    protected final AtomicReference<Optional<FKGame>> optFKGameRef;

    public MyCmd2(AtomicReference<Optional<FKGame>> optFKGameRef) {
        this.optFKGameRef = optFKGameRef;
    }
}
