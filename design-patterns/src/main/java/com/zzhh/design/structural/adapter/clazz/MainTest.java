package com.zzhh.design.structural.adapter.clazz;

import com.zzhh.design.structural.adapter.MoviePlayer;

/**
 * @ClassName MainTest
 * @Description <description class purpose>
 * @Author zhanghao
 * @Create 2023年02月21日 13:54:19
 */
public class MainTest {

    public static void main(String[] args) {
        MoviePlayer moviePlayer = new MoviePlayer();

        JPMoviePlayerAdapter jpMoviePlayerAdapter = new JPMoviePlayerAdapter(moviePlayer);

        jpMoviePlayerAdapter.play();

    }
}
