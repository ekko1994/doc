package com.zzhh.design.structural.adapter.obj;

import com.zzhh.design.structural.adapter.Player;
import com.zzhh.design.structural.adapter.Translator;
import com.zzhh.design.structural.adapter.Zh_JPTranslator;

/**
 * @ClassName JPMoviePlayerAdapter
 * @Description
 * @Author zhanghao
 * @Create 2023年02月21日 14:00:44
 */
public class JPMoviePlayerAdapter implements Player {

    //组合的方式
    private Translator translator = new Zh_JPTranslator();

    // 被适配对象
    private Player target;

    public JPMoviePlayerAdapter(Player target) {
        this.target = target;
    }

    @Override
    public String play() {
        String play = target.play();
        //转换字幕
        String translate = translator.translate(play);
        System.out.println("日文字幕：" +translate);
        return play;
    }
}
