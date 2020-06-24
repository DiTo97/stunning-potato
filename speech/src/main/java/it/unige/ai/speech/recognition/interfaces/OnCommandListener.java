package it.unige.ai.speech.recognition.interfaces;

public interface OnCommandListener {

    public void onTimeout();

    public void onNotRecognized();

    public void onSupported(String cmd);

}
