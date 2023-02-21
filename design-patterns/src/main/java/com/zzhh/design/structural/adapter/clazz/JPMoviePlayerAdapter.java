package com.zzhh.design.structural.adapter.clazz;

import com.zzhh.design.structural.adapter.Player;
import com.zzhh.design.structural.adapter.Zh_JPTranslator;

/**
 * @ClassName JPMoviePlayerAdapter
 * @Description 继承的方式：适配转换到翻译机的功能上
 * @Author zhanghao
 * @Create 2023年02月21日 13:50:18
 */
public class JPMoviePlayerAdapter extends Zh_JPTranslator implements Player {

    // 被适配对象
    private Player target;

    public JPMoviePlayerAdapter(Player target){
        this.target = target;
    }

    @Override
    public String play() {
        String play = target.play();
        //转换字幕
        String translate = translate(play);
        System.out.println("日文字幕：" +translate);
        return play;
    }
}
