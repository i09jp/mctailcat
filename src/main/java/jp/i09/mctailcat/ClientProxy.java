package jp.i09.mctailcat;

public class ClientProxy extends CommonProxy {

@Override
public void init(FMLInitializationEvent event) {
    super.init(event);

    final String testAddress =
        System.getenv("MCTAILCAT_TEST_ADDR");

    if (testAddress == null || testAddress.isEmpty()) {
        return;
    }

    Thread thread = new Thread(
        new Runnable() {

            @Override
            public void run() {
                try {
                    int port =
                        TailcatForwarder.start(testAddress);

                    System.out.println(
                        "[MCtailcat] Tailcat ready: "
                            + "127.0.0.1:"
                            + port
                    );

                } catch (Exception e) {
                    System.err.println(
                        "[MCtailcat] Tailcat failed"
                    );
                    e.printStackTrace();
                }
            }
        },
        "MCtailcat-test"
    );

    thread.setDaemon(true);
    thread.start();
}

}
