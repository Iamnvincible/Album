package com.jude.album.model;

import android.content.Context;

import com.jude.album.domain.Dir;
import com.jude.album.domain.body.Exist;
import com.jude.album.domain.body.Info;
import com.jude.album.domain.entities.User;
import com.jude.album.model.server.DaggerServiceModelComponent;
import com.jude.album.model.server.ErrorTransform;
import com.jude.album.model.server.HeaderInterceptors;
import com.jude.album.model.server.SchedulerTransform;
import com.jude.album.model.server.ServiceAPI;
import com.jude.beam.model.AbsModel;
import com.jude.utils.JFileManager;
import com.jude.utils.JUtils;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * Created by zhuchenxi on 16/1/21.
 */
public class AccountModel extends AbsModel {
    private static final String FILE_ACCOUNT = "account";
    @Inject
    ServiceAPI mServiceAPI;

    public static AccountModel getInstance() {
        return getInstance(AccountModel.class);
    }


    private BehaviorSubject<User> mAccountSubject = BehaviorSubject.create();


    @Override
    protected void onAppCreate(Context ctx) {
        super.onAppCreate(ctx);
        DaggerServiceModelComponent.builder().build().inject(this);
        //账号持久化
        mAccountSubject.subscribe(account -> {
            if (account==null) JFileManager.getInstance().getFolder(Dir.Object).deleteChild(FILE_ACCOUNT);
            else {
                JUtils.Log(account.toString());
                JFileManager.getInstance().getFolder(Dir.Object).writeObjectToFile(account,FILE_ACCOUNT);
            }
        });
        //token设置
        mAccountSubject.subscribe(account1 -> {
            if (account1!=null) {
                HeaderInterceptors.TOKEN = account1.getToken();
                HeaderInterceptors.UID = account1.getId() + "";
            }else {
                HeaderInterceptors.TOKEN = "";
                HeaderInterceptors.UID = "";
            }
            JUtils.Log("Set Token:"+HeaderInterceptors.TOKEN);
        });
        //初始化账户
        Observable.just((User) JFileManager.getInstance().getFolder(Dir.Object).readObjectFromFile(FILE_ACCOUNT))
                .doOnNext(account -> mAccountSubject.onNext(account))
                .subscribe();

    }

    public User getCurrentAccount(){
        return mAccountSubject.getValue();
    }


    public BehaviorSubject<User> getAccountSubject(){
        return mAccountSubject;
    }

    public boolean hasLogin(){
        return mAccountSubject.getValue()!=null;
    }


    public Observable<User> login(String account, String password){
        return mServiceAPI.login(account,password)
                .compose(new SchedulerTransform<>())
                .doOnNext(account1 -> mAccountSubject.onNext(account1));
    }

    public Observable<Info> register(String number, String password, String name, String code){
        return mServiceAPI.register(number, name,password, code).compose(new SchedulerTransform<>());
    }

    public Observable<Object> modifyPassword(String number, String password, String code){
        return mServiceAPI.modifyPassword(number, password, code).compose(new SchedulerTransform<>());
    }

    public Observable<Boolean> checkAccount(String number){
        return mServiceAPI.checkAccountExist(number).compose(new SchedulerTransform<>()).map(Exist::isExist);
    }



    public void refreshAccount(){
        mServiceAPI.refreshAccount()
                .compose(new SchedulerTransform<>())
                .compose(new ErrorTransform<>(ErrorTransform.ServerErrorHandler.NONE))
                .subscribe(account -> mAccountSubject.onNext(account));
    }
//
//    public Observable<Info> edit(String name, String intro, String avatar, long birth, int gender){
//        return mServiceAPI.edit(name, intro, birth, gender, avatar).compose(new SchedulerTransform<>())
//                .doOnNext( info -> refreshAccount());
//    }

    public void logout(){
        mAccountSubject.onNext(null);
    }

}
