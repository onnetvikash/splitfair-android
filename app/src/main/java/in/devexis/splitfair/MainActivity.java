package in.devexis.splitfair;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.text.NumberFormat;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_PEOPLE = "people";
    private static final int MIN_PEOPLE = 1;
    private static final int MAX_PEOPLE = 50;

    private final NumberFormat money = NumberFormat.getCurrencyInstance();

    private int people = 2;
    private SplitCalculator.Result lastResult;

    private TextInputEditText billInput;
    private Slider tipSlider;
    private SwitchCompat roundSwitch;
    private TextView tipLabel;
    private TextView peopleCount;
    private TextView tipValue;
    private TextView totalValue;
    private TextView perPersonValue;
    private TextView extraNote;
    private MaterialButton minusButton;
    private MaterialButton plusButton;
    private MaterialButton shareButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        billInput = findViewById(R.id.billInput);
        tipSlider = findViewById(R.id.tipSlider);
        roundSwitch = findViewById(R.id.roundSwitch);
        tipLabel = findViewById(R.id.tipLabel);
        peopleCount = findViewById(R.id.peopleCount);
        tipValue = findViewById(R.id.tipValue);
        totalValue = findViewById(R.id.totalValue);
        perPersonValue = findViewById(R.id.perPersonValue);
        extraNote = findViewById(R.id.extraNote);
        minusButton = findViewById(R.id.minusButton);
        plusButton = findViewById(R.id.plusButton);
        shareButton = findViewById(R.id.shareButton);

        if (savedInstanceState != null) {
            people = savedInstanceState.getInt(KEY_PEOPLE, people);
        }

        billInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                update();
            }
        });
        tipSlider.addOnChangeListener((slider, value, fromUser) -> update());
        roundSwitch.setOnCheckedChangeListener((button, checked) -> update());
        minusButton.setOnClickListener(v -> {
            if (people > MIN_PEOPLE) {
                people--;
                update();
            }
        });
        plusButton.setOnClickListener(v -> {
            if (people < MAX_PEOPLE) {
                people++;
                update();
            }
        });
        shareButton.setOnClickListener(v -> share());

        update();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_PEOPLE, people);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        update();
    }

    private void update() {
        CharSequence typed = billInput.getText();
        long bill = SplitCalculator.parseCents(typed == null ? null : typed.toString());
        int tipPercent = Math.round(tipSlider.getValue());

        lastResult = SplitCalculator.calculate(bill, tipPercent, people, roundSwitch.isChecked());

        tipLabel.setText(getString(R.string.tip_label, tipPercent));
        peopleCount.setText(String.valueOf(people));
        tipValue.setText(format(lastResult.tipCents));
        totalValue.setText(format(lastResult.totalCents));
        perPersonValue.setText(format(lastResult.perPersonCents));

        if (lastResult.extraCents > 0) {
            extraNote.setText(getString(R.string.extra_note, format(lastResult.extraCents)));
            extraNote.setVisibility(TextView.VISIBLE);
        } else {
            extraNote.setVisibility(TextView.GONE);
        }

        minusButton.setEnabled(people > MIN_PEOPLE);
        plusButton.setEnabled(people < MAX_PEOPLE);
        shareButton.setEnabled(lastResult.billCents > 0);
    }

    private void share() {
        if (lastResult == null || lastResult.billCents <= 0) {
            return;
        }
        int tipPercent = Math.round(tipSlider.getValue());
        String summary = getString(R.string.summary,
                format(lastResult.billCents),
                format(lastResult.tipCents),
                tipPercent,
                format(lastResult.totalCents),
                lastResult.people,
                format(lastResult.perPersonCents));

        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, summary);
        startActivity(Intent.createChooser(send, getString(R.string.share_title)));
    }

    private String format(long cents) {
        return money.format(BigDecimal.valueOf(cents, 2));
    }
}
