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
    private View homeScreen, detailScreen;
    private PlanetView systemView, detailPlanetView;
    private PlanetOrderView orderView;
    private Button orbitButton;
    private int questionIndex=0, score=0;
    private boolean answered=false;

    private final String[][] questions={
        {"Which planet is known as the Red Planet?","Venus","Mars","Neptune","1"},
        {"Which planet is the biggest?","Earth","Mercury","Jupiter","2"},
        {"Which planet has beautiful rings?","Saturn","Mars","Venus","0"},
        {"Which planet do we live on?","Uranus","Earth","Jupiter","1"},
        {"What is at the center of our solar system?","The Moon","Mars","The Sun","2"}
    };
    private final String[] names={"The Sun","Mercury","Venus","Earth","Mars","Jupiter","Saturn","Uranus","Neptune"};
    private final String[] facts={
        "The Sun is a star at the center of our solar system. Its light and heat help make life on Earth possible.",
        "Mercury is the smallest planet and the closest planet to the Sun. Its surface has many craters.",
        "Venus is wrapped in thick clouds and is the hottest planet. Its surface is hot enough to melt lead.",
        "Earth is our home planet. It has liquid water on its surface and is the only world we know has life.",
        "Mars is called the Red Planet because iron-rich dust gives its surface a rusty color. It has enormous volcanoes.",
        "Jupiter is the biggest planet. Its Great Red Spot is a giant storm that has raged for hundreds of years.",
        "Saturn is famous for its beautiful rings, made mostly of countless pieces of ice and rock.",
        "Uranus is an icy giant that rotates almost on its side. It has a faint ring system.",
        "Neptune is a distant, deep-blue ice giant with extremely fast winds."
    };
    private final String[] fun={
        "A beam of sunlight takes about 8 minutes to reach Earth.",
        "A year on Mercury lasts only 88 Earth days.",
        "Venus is hotter than Mercury, even though Mercury is closer to the Sun.",
        "About 71% of Earth's surface is covered by water.",
        "Olympus Mons on Mars is the largest known volcano in the solar system.",
        "More than 1,300 Earths could fit inside Jupiter.",
        "Saturn is less dense than water, although there is no bathtub big enough!",
        "One year on Uranus lasts 84 Earth years.",
        "Neptune takes about 165 Earth years to orbit the Sun."
    };
    private final String[][] worldFeatures={
        {"The Sun is a huge ball of hot gas. Its energy travels through space as sunlight.","The Sun’s surface has cooler, darker patches called sunspots.","The Sun’s gravity keeps the planets moving around it."},
        {"Mercury has many impact craters, made when space rocks hit its surface.","Mercury has almost no atmosphere to hold in heat.","A year on Mercury is only 88 Earth days."},
        {"Venus is covered by thick clouds of sulfuric acid.","Its atmosphere traps heat, making Venus the hottest planet.","Venus spins very slowly and in the opposite direction to most planets."},
        {"Earth’s oceans cover about 71% of its surface.","Our atmosphere helps protect life and keeps Earth warm enough.","Earth’s Moon helps create ocean tides."},
        {"Mars has giant volcanoes, including Olympus Mons.","The reddish color comes from iron-rich dust, like rust.","Mars has two small moons: Phobos and Deimos."},
        {"Jupiter’s Great Red Spot is a gigantic storm.","Jupiter has faint rings made of dust.","Jupiter has many moons, including icy Europa."},
        {"Saturn’s rings are made of countless pieces of ice and rock.","The rings are very wide but surprisingly thin.","Saturn has many moons, including Titan."},
        {"Uranus rotates almost on its side.","Its blue-green color comes from methane in the atmosphere.","Uranus has faint rings and many moons."},
        {"Neptune has extremely fast winds.","Its deep blue color comes partly from gases in its atmosphere.","Neptune is so far away that one year lasts 165 Earth years."}
    };

    private final String[] discovered={
        "Known since prehistoric times. People have observed the Sun for thousands of years; there is no single known discoverer.",
        "Known since ancient times. It can be seen with the naked eye, so its first observer is unknown.",
        "Known since ancient times. Ancient civilizations recorded Venus; no single discoverer is known.",
        "Known since prehistoric times. Earth was our home long before people understood it was a planet.",
        "Known since ancient times. Ancient Egyptian and other astronomers recorded Mars; its first observer is unknown.",
        "Known since ancient times. It is bright enough to see without a telescope; its first observer is unknown.",
        "Known since ancient times. Ancient observers recorded Saturn; its first observer is unknown.",
        "Discovered on 13 March 1781 by astronomer William Herschel using a telescope. It was the first planet discovered in modern history.",
        "Observed on 23 September 1846 by Johann Galle, after mathematicians predicted where it should be. It was the first planet found using mathematical prediction."
    };

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(9,13,40));
        getWindow().setNavigationBarColor(Color.rgb(9,13,40));
        setContentView(R.layout.activity_main);
        homeScreen=findViewById(R.id.homeScreen);
        detailScreen=findViewById(R.id.detailScreen);
        planetName=findViewById(R.id.planetName);planetFact=findViewById(R.id.planetFact);planetExtra=findViewById(R.id.planetExtra);
        quizQuestion=findViewById(R.id.quizQuestion);quizFeedback=findViewById(R.id.quizFeedback);answers=findViewById(R.id.answers);
        systemView=findViewById(R.id.planetView);detailPlanetView=findViewById(R.id.detailPlanetView);
        orderView=findViewById(R.id.orderPuzzle);
        orbitButton=findViewById(R.id.orbitButton);
        orderView.setOnOrderCorrectListener(correct -> orbitButton.setVisibility(correct ? View.VISIBLE : View.GONE));
        orbitButton.setOnClickListener(v -> { systemView.startOrbitAnimation(); v.setText("Planets are orbiting! ✨"); });
        systemView.setOnPlanetSelectedListener(this::showPlanet);
        systemView.setOnPlanetOpenedListener(this::openPlanet);
        detailPlanetView.setOnWorldFeatureListener((planet, feature) -> {
            String[] items = worldFeatures[planet];
            ((TextView)findViewById(R.id.detailWorldInfo)).setText("🔎 " + items[feature]);
        });
        findViewById(R.id.closeDetail).setOnClickListener(v->closePlanet());
        findViewById(R.id.nextQuestion).setOnClickListener(v->nextQuestion());
        showPlanet(0);showQuestion();
    }

    private void showPlanet(int i){
        planetName.setText(i==0?"☀️ "+names[i]:"🪐 "+names[i]);
        planetFact.setText(facts[i]);planetExtra.setText("Fun fact: "+fun[i]);
    }
    private void openPlanet(int i){
        detailPlanetView.showPlanetDetail(i);
        ((TextView)findViewById(R.id.detailTitle)).setText(names[i]);
        ((TextView)findViewById(R.id.detailDescription)).setText(facts[i]);
        ((TextView)findViewById(R.id.detailFunFact)).setText("✨ "+fun[i]);
        ((TextView)findViewById(R.id.detailDiscovery)).setText("🔭 Discovery story\n"+discovered[i]);
        homeScreen.setVisibility(View.GONE);detailScreen.setVisibility(View.VISIBLE);
    }
    private void closePlanet(){
        detailScreen.setVisibility(View.GONE);homeScreen.setVisibility(View.VISIBLE);systemView.showSolarSystem();
    }

    private void showQuestion(){
        answered=false;String[] q=questions[questionIndex];
        quizQuestion.setText("Question "+(questionIndex+1)+" of "+questions.length+"\n"+q[0]);
        quizFeedback.setText("Pick the best answer!");answers.removeAllViews();
        for(int i=1;i<=3;i++){
            final int choice=i-1;Button b=new Button(this);b.setAllCaps(false);b.setText(q[i]);b.setTextSize(16);b.setTextColor(Color.WHITE);
            b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(48,66,116)));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=7;answers.addView(b,lp);
            b.setOnClickListener(v->checkAnswer(choice));
        }
    }
    private void checkAnswer(int choice){
        if(answered)return;answered=true;boolean correct=choice==Integer.parseInt(questions[questionIndex][4]);if(correct)score++;
        quizFeedback.setText(correct?"🎉 Brilliant! That's right! Score: "+score:"💫 Good try! The correct answer is "+questions[questionIndex][Integer.parseInt(questions[questionIndex][4])+1]+". Score: "+score);
    }
    private void nextQuestion(){
        questionIndex++;
        if(questionIndex>=questions.length){
            quizQuestion.setText("Quiz complete! 🌟");answers.removeAllViews();
            quizFeedback.setText("You got "+score+" out of "+questions.length+" correct. "+(score==questions.length?"Amazing space expert!":"Great exploring! Try again to beat your score."));
            Button restart=new Button(this);restart.setText("Play again");restart.setAllCaps(false);answers.addView(restart);
            restart.setOnClickListener(v->{questionIndex=0;score=0;showQuestion();});
        }else showQuestion();
    }
}
