/*
 * Applitools SDK for Selenium integration.
 */
package com.applitools.eyes;

/**
 * A base class for triggers.
 */
abstract class Trigger {

    @SuppressWarnings("UnusedDeclaration")
    enum TriggerType {Unknown, Mouse, Text, Keyboard}

    @SuppressWarnings("UnusedDeclaration")
    public abstract TriggerType getTriggerType();
}