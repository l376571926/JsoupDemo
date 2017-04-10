package com.aoemo.jsoupdemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.socks.library.KLog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ExecutorService mCachedThreadPool;
    private TextView mTextView;
    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.textViewTv);
        mEditText = (EditText) findViewById(R.id.editTextEt);

        findViewById(R.id.buttonBtn).setOnClickListener(this);
        findViewById(R.id.buttonBtn1).setOnClickListener(this);

        mCachedThreadPool = Executors.newCachedThreadPool();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String obj = (String) msg.obj;
            mTextView.append(obj);
            mTextView.append("\n");
        }
    };

    Runnable runnable = new Runnable() {
        public static final String TAG = "Runnable";
        private static final String mTargetUrl = "https://www.baidu.com/";

        //        private static final String mTargetUrl_1 = "http://221.236.35.60/about.aspx?Mid=265";//达标获奖
//        private static final String mTargetUrl_1 = "http://221.236.35.60/News.aspx?Mid=110";//政策法规
//        private static final String mTargetUrl_1 = "http://221.236.35.60/SadcList.aspx?Mid=271";//网上调查
//        private static final String mTargetUrl_1 = "http://221.236.35.60/about.aspx?Mid=204";//本馆简介
//        private static final String mTargetUrl_1 = "http://221.236.35.60/ArchiveSearch.aspx?Mid=276";//在线查档
//        private static final String mTargetUrl_1 = "http://221.236.35.60/Send.aspx?Mid=270";//在线咨询
//        private static final String mTargetUrl_1 = "http://221.236.35.60/Collection.aspx?Mid=272";//网上征集
//        private static final String mTargetUrl_1 = "http://221.236.35.60/about.aspx?Mid=231";//办事指南
        private static final String mTargetUrl_1 = "http://221.236.35.60/News.aspx?Mid=268";//审查公告

        @Override
        public void run() {
            try {
                Document document = Jsoup.connect(mTargetUrl_1).get();
                String title = document.title();//绵阳市城建档案馆

                //标题
                Elements zwnr = document.getElementsByClass("zwnr");
                for (Element element : zwnr) {
                    Elements h2 = element.getElementsByTag("h2");
                    for (Element element1 : h2) {
                        String text = element1.text();
                        Message message = Message.obtain();
                        message.obj = text;
                        mHandler.sendMessage(message);
                        KLog.e(TAG, "标题: " + text);
                    }
                }

                //文章来源
                Elements wzly = document.getElementsByClass("wzly");
                for (Element element : wzly) {
                    Elements span = element.getElementsByTag("span");
                    for (Element element1 : span) {
                        String text = element1.text();
                        Message message = Message.obtain();
                        message.obj = text;
                        mHandler.sendMessage(message);
                        KLog.e(TAG, "文章来源: " + text);
                    }
                }

                //列表
                Elements wzlist = document.getElementsByClass("wzlist");
                for (Element element : wzlist) {
                    Elements li = element.getElementsByTag("li");
                    for (Element element12 : li) {
                        Elements a = element12.getElementsByTag("a");
                        for (Element element1 : a) {
                            String text = element1.text();
                            Message message = Message.obtain();
                            message.obj = text;
                            mHandler.sendMessage(message);
                            KLog.e(TAG, "列表: " + text);
                        }
                        String lastSubmitString = null;
                        Elements span = element12.getElementsByTag("span");
                        for (Element element1 : span) {
                            String text = element1.text();
                            if (TextUtils.equals(lastSubmitString, text)) {
                                continue;
                            }
                            lastSubmitString = text;
                            Message message = Message.obtain();
                            message.obj = text;
                            mHandler.sendMessage(message);
                            KLog.e(TAG, "列表: " + text);
                        }
                    }
                }

                //内容
                Elements elements = document.getElementsByClass("content_wz");
                String lastSubmitString = null;
                for (Element element : elements) {
                    Elements allElements = element.getAllElements();
                    for (Element allElement : allElements) {
                        String text = allElement.text();
                        if (TextUtils.equals(lastSubmitString, text)) {
                            continue;
                        }
                        lastSubmitString = text;
                        Message message = Message.obtain();
                        message.obj = text;
                        mHandler.sendMessage(message);
                        KLog.e(TAG, "内容: " + text);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                KLog.e(TAG, "run: " + e);
            }
        }
    };

    /**
     * 抓取 https://www.tenor.co/ 中的实时热门Gif搜索关键字
     */
    private static class TenorTrendingRunnable implements Runnable {
        private int id;
        private OnParserFinishListener onParserFinishListener;

        public TenorTrendingRunnable(int id, OnParserFinishListener onParserFinishListener) {
            this.id = id;
            this.onParserFinishListener = onParserFinishListener;
        }

        @Override
        public void run() {
            try {
                Document document = Jsoup.connect("https://www.tenor.co/").get();
                Elements elements = document.getElementsByClass("related-tag");
                List<String> tagList = new ArrayList<>();
                for (Element element : elements) {
                    Elements name = element.getElementsByClass("name");
                    for (Element element1 : name) {
                        String related_tag = element1.text();
                        tagList.add(related_tag);
                    }
                }
                KLog.e(tagList.toString());
                if (!tagList.isEmpty()) {
                    onParserFinishListener.onFinish(id, tagList);
                    onParserFinishListener = null;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonBtn:
                TenorTrendingRunnable trendingRunnable = new TenorTrendingRunnable(123, new OnParserFinishListener() {
                    @Override
                    public void onFinish(int id, List list) {
                        KLog.e(list.toString());
                    }
                });
                mCachedThreadPool.submit(trendingRunnable);

//                mCachedThreadPool.submit(runnable);
                break;
            case R.id.buttonBtn1:
                String s = mEditText.getText().toString();
                if (TextUtils.isEmpty(s)) {
                    mTextView.append("\n");
                } else {
                    mTextView.append(s);
                    mTextView.append("\n");
                }
                break;
            default:
                break;
        }
    }

    public interface OnParserFinishListener {
        void onFinish(int id, List list);
    }
}
