package com.axelwiston.solarsystemexplorer;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private TextView planetName, planetFact, planetExtra, quizQuestion, quizFeedback;
    private LinearLayout answers;
    private int questionIndex = 0;
    private int score = 0;
    private boolean answered = false;

    private final String[][] questions = {
        {"Which planet is known as the Red Planet?", "Venus", "Mars", "Neptune", "1"},
        {"Which planet is the biggest?", "Earth", "Mercury", "Jupiter", "2"},
        {"Which planet has beautiful rings?", "Saturn", "Mars", "Venus", "0"},
        {"Which planet do we live on?", "Uranus", "Earth", "Jupiter", "1"},
        {"What is at the center of our solar system?", "The Moon", "Mars", "The Sun", "2"}
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(9,13,40));
        getWindow().setNavigationBarColor(Color.rgb(9,13,40));
        setContentView(R.layout.activity_main);
        planetName = findViewById(R.id.planetName);
        planetFact = findViewById(R.id.planetFact);
        planetExtra = findViewById(R.id.planetExtra);
        quizQuestion = findViewById(R.id.quizQuestion);
        quizFeedback = findViewById(R.id.quizFeedback);
        answers = findViewById(R.id.answers);
        PlanetView view = findViewById(R.id.planetView);
        view.setOnPlanetSelectedListener(this::showPlanet);
        findViewById(R.id.nextQuestion).setOnClickListener(v -> nextQuestion());
        showPlanet(0);
        showQuestion();
    }

    private void showPlanet(int index) {
        String[] names = {"The Sun","Mercury","Venus","Earth","Mars","Jupiter","Saturn","Uranus","Neptune"};
        String[] facts = {
            "The Sun is a star. It gives our solar system light and heat!",
            "Mercury is the smallest planet and the closest planet to the Sun.",
            "Venus is super hot and has thick, cloudy skies.",
            "Earth is our home. It has liquid water and lots of living things.",
            "Mars looks red because of rusty dust on its surface.",
            "Jupiter is the biggest planet. A giant storm called the Great Red Spot swirls there!",
            "Saturn has amazing rings made mostly of bits of ice and rock.",
            "Uranus spins on its side. It is a chilly, blue-green ice giant.",
            "Neptune is a deep-blue, windy planet far from the Sun."
        };
        String[] extras = {
            "Fun fact: Light from the Sun takes about 8 minutes to reach Earth.",
            "Fun fact: A year on Mercury is just 88 Earth days.",
            "Fun fact: Venus is the hottest planet, even hotter than Mercury.",
            "Fun fact: About 71% of Earth's surface is covered by water.",
            "Fun fact: Mars has a huge volcano called Olympus Mons.",
            "Fun fact: More than 1,300 Earths could fit inside Jupiter.",
            "Fun fact: Saturn is so light for its size that it would float in a giant bathtub!",
            "Fun fact: Uranus takes 84 Earth years to orbit the Sun.",
            "Fun fact: Neptune takes about 165 Earth years to go around the Sun."
        };
        planetName.setText(index == 0 ? "☀️ " + names[index] : "🪐 " + names[index]);
        planetFact.setText(facts[index]);
        planetExtra.setText(extras[index]);
    }

    private void showQuestion() {
        answered = false;
        String[] q = questions[questionIndex];
        quizQuestion.setText("Question " + (questionIndex + 1) + " of " + questions.length + "\n" + q[0]);
        quizFeedback.setText("Pick the best answer!");
        answers.removeAllViews();
        for (int i=1; i<=3; i++) {
            final int choice = i-1;
            Button b = new Button(this);
            b.setAllCaps(false);
            b.setText(q[i]);
            b.setTextSize(16);
            b.setTextColor(Color.WHITE);
            b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(48,66,116)));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.bottomMargin = 7;
            answers.addView(b, lp);
            b.setOnClickListener(v -> checkAnswer(choice));
        }
    }

    private void checkAnswer(int choice) {
        if (answered) return;
        answered = true;
        boolean correct = choice == Integer.parseInt(questions[questionIndex][4]);
        if (correct) score++;
        quizFeedback.setText(correct ? "🎉 Brilliant! That's right! Score: " + score : "💫 Good try! The correct answer is " + questions[questionIndex][Integer.parseInt(questions[questionIndex][4])+1] + ". Score: " + score);
    }

    private void nextQuestion() {
        questionIndex++;
        if (questionIndex >= questions.length) {
            quizQuestion.setText("Quiz complete! 🌟");
            answers.removeAllViews();
            quizFeedback.setText("You got " + score + " out of " + questions.length + " correct. " + (score == questions.length ? "Amazing space expert!" : "Great exploring! Try again to beat your score."));
            Button restart = new Button(this);
            restart.setText("Play again");
            restart.setAllCaps(false);
            answers.addView(restart);
            restart.setOnClickListener(v -> { questionIndex=0; score=0; showQuestion(); });
        } else showQuestion();
    }
}
