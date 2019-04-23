package com.packt;

public interface Gpio {

    void addListener(Listener listener);

    interface Listener {
        void pinChanged(String pinNumber);
		void pinsConfigured();
    }
}
