package org.dicio.dicio_android.input;

import android.util.Log;

import androidx.annotation.Nullable;

import org.dicio.dicio_android.BuildConfig;
import org.dicio.skill.util.CleanableUp;

public abstract class InputDevice implements CleanableUp {

    /**
     * Used to provide the input from the user to whatever code uses it
     */
    public interface InputDeviceListener {

        /**
         * Called when the user provided some partial input (e.g. while talking)
         * @param input the received partial input
         */
        void onPartialInputReceived(String input);

        /**
         * Called when some input was received from the user
         * @param input the received input
         */
        void onInputReceived(String input);

        /**
         * Called when no input was received from the user after he seemed to want to provide some
         */
        void onNoInputReceived();

        /**
         * Called when an error occurs while trying to get input or processing it
         * @param e the exception
         */
        void onError(Throwable e);
    }

    private static final String TAG = InputDevice.class.getSimpleName();

    @Nullable
    private InputDeviceListener inputDeviceListener = null;


    /**
     * Prepares the input device. If doing heavy work, run it in an asynchronous thread.
     * <br><br>
     * Overriding functions should report errors to {@link #notifyError(Throwable)}.
     */
    public abstract void load();

    /**
     * Tries to get input in any way. Should run in an asynchronous thread.
     * <br><br>
     * Overriding functions should report partial results to {@link
     * #notifyPartialInputReceived(String)}, final results to {@link #notifyInputReceived(String)}
     * or {@link #notifyNoInputReceived()} (based on whether some input was received or not) and
     * errors to {@link #notifyError(Throwable)}.
     */
    public abstract void tryToGetInput();

    /**
     * Cancels any input being received after {@link #tryToGetInput()} was called. Should do nothing
     * if called while not getting input. Any partial input is discarded. Called for
     * example when the user leaves the app.
     * <br><br>
     * Overriding functions should call {@link #notifyNoInputReceived()} when they stop getting
     * input.
     */
    public abstract void cancelGettingInput();

    /**
     * Sets the listener, used to provide the input from the user to whatever code uses it
     * @param listener the listener to use, set it to {@code null} to remove it
     */
    public final void setInputDeviceListener(@Nullable final InputDeviceListener listener) {
        this.inputDeviceListener = listener;
    }

    @Override
    public void cleanup() {
        setInputDeviceListener(null);
    }


    /**
     * This has to be called by functions overriding {@link #tryToGetInput()} when some input from
     * the user is received. Can be called multiple times while getting input.
     * @param input the (raw) received input
     */
    protected void notifyPartialInputReceived(final String input) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Partial input from user: " + input);
        }

        if (inputDeviceListener != null) {
            inputDeviceListener.onPartialInputReceived(input);
        }
    }

    /**
     * This has to be called by functions overriding {@link #tryToGetInput()} when some input from
     * the user is received. Should be called only once, when stopping to get input. Should not be
     * called if {@link #notifyNoInputReceived()} is called instead.
     * @param input the (raw) received input
     */
    protected void notifyInputReceived(final String input) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Input from user: " + input);
        }

        if (inputDeviceListener != null) {
            inputDeviceListener.onInputReceived(input);
        }
    }

    /**
     * This has to be called by functions overriding {@link #tryToGetInput()} when the user provided
     * no input. Should be called only once, when stopping to get input. Should not be
     * called if {@link #notifyInputReceived(String)} is called instead.
     */
    protected void notifyNoInputReceived() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "No input from user");
        }

        if (inputDeviceListener != null) {
            inputDeviceListener.onNoInputReceived();
        }
    }

    /**
     * This has to be called by functions overriding {@link #tryToGetInput()} when the user sent
     * some input, but it could not be processed due to an error. This can also be called if there
     * was an error while loading, and can be called by functions overriding {@link #load()}.
     * @param e an exception to handle
     */
    protected void notifyError(final Throwable e) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Input error: " + e.getMessage(), e);
        }

        if (inputDeviceListener != null) {
            inputDeviceListener.onError(e);
        }
    }
}
