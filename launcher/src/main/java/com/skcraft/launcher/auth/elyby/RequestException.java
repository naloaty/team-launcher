/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.auth.elyby;

import com.skcraft.launcher.LauncherException;

/**
 * Thrown on Ely.by request error.
 */
public class RequestException extends LauncherException {
    public RequestException(String message, String localizedMessage) {
        super(message, localizedMessage);
    }

    public RequestException(String message) {
        super(message, message);
    }

    public RequestException(Throwable cause, String localizedMessage) {
        super(cause, localizedMessage);
    }
}
