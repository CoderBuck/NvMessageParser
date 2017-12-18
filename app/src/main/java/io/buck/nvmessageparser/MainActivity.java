package io.buck.nvmessageparser;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import io.buck.parser.Message;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Message(Msg.aaa)
    public static void a(byte[] a) {

    }

    @Message(Msg.aaa)
    public static void b(byte[] a) {

    }
}
