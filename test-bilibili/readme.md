会有延迟
http://api.bilibili.com/userinfo?user=xzchaooDR4

先登陆再访问 就可以签到了!
http://api.bilibili.com/myinfo?access_key=5be9b8caed10c4cfe1e34bed8b3d247c


5be9b8caed10c4cfe1e34bed8b3d247c


duruofeixh5@163.com 5be9b8caed10c4cfe1e34bed8b3d247c
duruofeixh6@163.com access_key=c33da748da58b980a3089d8001f2ff2c
duruofeixh7@163.com d89fa82e6dec663949f89abdf56d8ad1


http://space.bilibili.com/ajax/member/GetInfo?mid=19539291
http://api.bilibili.cn/userinfo?user=xuzhichaoxh1


测试用账号 duruofeixh8@163.com
aid = 19539141 fid = 19796220

# 视频 #
最小化登陆
https://account.bilibili.com/ajax/miniLogin/minilogin
可以不需要验证码!

添加到收藏夹
get请求 添加到默认的收藏夹 需要带cookie
http://api.bilibili.com/favourite/add?id=3335348
post请求 添加到指定的收藏夹 需要带cookie
http://api.bilibili.com/x/favourite/video/add?jsonp=jsonp&fid=19764585&aid=3425515

获取用户信息
http://api.bilibili.com/userinfo?user=xzchaooDRF8
http://api.bilibili.com/userinfo?mid=1655915
http://api.bilibili.com/myinfo?access_key=5be9b8caed10c4cfe1e34bed8b3d247c
http://space.bilibili.com/ajax/member/GetInfo?mid=19557513

获取用户的收藏夹
这个似乎没有做权限认证, 所以可以获取其他人的收藏夹?
http://space.bilibili.com/ajax/fav/getBoxList?mid=19539141
和[获取收藏的视频]搭配使用可以获取别人的收藏信息!
http://api.bilibili.com/x/favourite/folder 这个也行 需要带cookie

收藏列表 xzchaooDRF8
http://space.bilibili.com/ajax/fav/getList?mid=19557513&pagesize=30&fid=19764585
pagesize最大在70左右

获得评论列表
http://api.bilibili.com/feedback?page=1&mode=arc&type=json&ver=3&order=default&pagesize=10&aid=3431351

获得视频信息
http://api.bilibili.cn/view?appkey=03fc8eb101b091fb&id=3342515
这个只能获得正常状态的视频信息
其他状态(无权限,已删除,不存在)会有code=404

删除收藏的视频
需要post操作, 还需要Refer头
http://space.bilibili.com/ajax/fav/mdel
fid:2424663
aids:3296072
返回值{"status":true}

# 视频类型 #
15 连载聚集
17 单机联机
20 宅舞
21 生活
22 三次元鬼畜
24 MAD·AMV
25 MMD·3D
26 二次元鬼畜
27综合
28 同人音乐
29 三次元音乐
30 VOCALOID·UTAU			**初音,天依**
31翻唱						**全部**
32 完结动画					**全部**
33 连载动画					**全部**
51 咨询
65 网游·电竞
71 综艺						**暴走大事件**
126 人力VOCALOID
128 电视剧相关
131 Korea相关
137 娱乐圈
138 搞笑					** 山下**
152 官方眼神
153 好像是国产吧?
154 三次元舞蹈

# 用户 #
获取用户信息
两种方式获取的数据基本一致, 推荐第二种
http://space.bilibili.com/ajax/member/GetInfo?mid=1643718
http://api.bilibili.cn/userinfo?user=xuzhichaoxh1
	或mid=?
	
# 数据库维护 #
通过下面这个语句获取某个时间点以前的视频， 虽然不是非常精确 但也是够用了， updateAt 一般是只我扫描到的时间
select aid from video where updateAt < '2015-12-09 00:00:00';

通过下面的一句将过期的任务（可能是已完成）删掉
delete from commenttask where aid in ( select aid from video where updateAt < '某个时间' );

通过下面的一句将视频删掉
delete from video where updateAt < '某个时间'

或者先将视频删除， 然后再执行下面的语句删除task
delete from commenttask where ait not in (select aid from video)


# API收藏 #
http://www.scriptsuser.org/code.php?hc=206
http://www.fuckbilibili.com/biliapi.html

获得每周的番剧更新
http://www.bilibili.com/index/index-bangumi-timeline.json

获取某个专题的信息
http://api.bilibili.cn/sp?spid=56746

查看某用户的投稿
http://space.bilibili.com/ajax/member/getSubmitVideos?mid=928123&pagesize=30&page=1



# 弹幕地址 #
http://comment.bilibili.com/5083274.xml
其中5083274是cid
想要获取cid可以通过访问
http://www.bilibili.com/video/av3223015/
然后抓取
<script type='text/javascript'>EmbedPlayer('player', "http://static.hdslb.com/play.swf", "cid=5083274&aid=3223015");</script>

需要提供
http://static.hdslb.com/play.swf?cid=5278762
好像提供aid也行, 不过该视频必须可以正常播放
离站播放这个也行
http://static.hdslb.com/miniloader.swf?aid=3342515&page=1


http://cn-zjhz3-dx.acgvideo.com/vg1/5/f5/5278763-1.flv?expires=1449474600&ssig=DPbFhUYY6iJtOabBeNnO7A&oi=1961149960&player=1&or=1902745807&rate=0

http://interface.bilibili.com/player?id=cid:5278763
http://61.164.47.167/player?id=cid:5278763

http://interface.bilibili.com/playurl?cid=5278763&player=1&ts=1449467419&sign=8be05bfa069879c44d6222e14d285e2a
http://comment.bilibili.com/cloud/filter/3337525.json

http://api.bilibili.com/list?pagesize=24&type=json&page=1&ios=0&order=default&appkey=03fc8eb101b091fb&platform=ios&tid=33
03fc8eb101b091fb

http://api.bilibili.cn/view?appkey=03fc8eb101b091fb&id=
http://api.bilibili.com/view?appkey=03fc8eb101b091fb&id=3336555&page=1&type=json
appkey=85eb6835b0a1034e
id=3327066
page=1
type=json
appkey=85eb6835b0a1034e&id=3327066&page=1&type=json
APPKEY = '85eb6835b0a1034e'
APPSEC = '2ad42749773c441109bdc0191257a664'

media_args = {'otype': 'json', 'cid': cid, 'type': 'flv', 'quality': 4, 'appkey': APPKEY}
otype=json&cid=5261323&type=flv&quality=4&appkey=03fc8eb101b091fb

通过cid获取其下载地址
http://interface.bilibili.com/playurl?otype=json&cid=5261323&type=flv&quality=4&appkey=03fc8eb101b091fb

发一次弹幕
请求头
POST /dmpost?cid=3374460&aid=2168384&pid=1 HTTP/1.1
Host: interface.bilibili.com
Connection: keep-alive
Content-Length: 180
Origin: http://static.hdslb.com
X-Requested-With: ShockwaveFlash/19.9.9.999
User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.87 Safari/537.36 QQBrowser/9.2.5584.400
Content-Type: application/x-www-form-urlencoded
Accept: */*
Referer: http://static.hdslb.com/play.swf
Accept-Encoding: gzip, deflate
Accept-Language: zh-CN,zh;q=0.8
Cookie: fts=1449328994; pgv_pvi=5697290240; tma=136533283.3407577.1449331165932.1449331165932.1449402200234.2; tmd=2.136533283.3407577.1449331165932.; pgv_si=s1714472960; sid=6rkknc2x; PLHistory=8qiK%7Coe<#[; DedeUserID=19216452; DedeUserID__ckMd5=2699614779cd474b; SESSDATA=704fe3e6%2C1450080146%2C50b903d1; LIVE_LOGIN_DATA=19aca27f0e458f5ed4e6d07812dc65bc46726ae2; LIVE_LOGIN_DATA__ckMd5=8a58dab19ada5f9f; _cnt_dyn=null; _cnt_pm=0; _cnt_notify=0; uTZ=-480; DedeID=2168384; _dfcaptcha=817d64b099cc6b2a2e538f315f05b10c

Accept	
text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
Accept-Encoding	
gzip, deflate
Accept-Language	
zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3
Connection	
keep-alive
Cookie	
DedeUserID=19161363; SESSDATA=b365258b,1449971602,80d03867
Host	
interface.bilibili.com
User-Agent	
Mozilla/5.0 (Windows NT 6.1; WOW64; rv:42.0) Gecko/20100101 Firefox/42.0

相应头
HTTP/1.1 200 OK
Date: Mon, 07 Dec 2015 08:40:08 GMT
Content-Type: text/html; charset=UTF-8
Transfer-Encoding: chunked
Connection: keep-alive
Server: Tengine
Access-Control-Allow-Credentials: true
Access-Control-Allow-Origin: http://m.acg.tv
Access-Control-Max-Age: 86400
X-Account-Via: MISS from shd-app-7
Set-Cookie: _cnt_pm=0; path=/; domain=.bilibili.com
X-SetDomain-Cg: .bilibili.com
X-SetDomain: .bilibili.com
Set-Cookie: _cnt_notify=0; path=/; domain=.bilibili.com
X-T4: 0.0017619132995605
X-AppCode: 0
X-SLB-Server: shd-slb-15
X-Cache: BYPASS from cn-gddg-dx.hdslb.com
Content-Encoding: gzip

Form Data
color:16777215
date:2015-12-07 16:40:10
cid:3374460
pool:0
mode:1
message:呵呵 真刺激.
rnd:761437893
playTime:628.993
fontsize:25


注册成功
http://www.bilibili.com/account/register_success

礼仪答题
https://account.bilibili.com/answer/base
https://account.bilibili.com/answer/getBaseQ 获得问题

提交
https://account.bilibili.com/answer/goPromotion post方式
ans_hash_43659	811260173aa28331ecf6e2ed3985c86c
ans_hash_43667	15a9ca748fcbc9296df4d4819a5897a4
ans_hash_43675	ce7204c4030211a43da491e8fed414e8
ans_hash_43679	b785d82ab875d9d07673603222cb2bbc
ans_hash_43681	c77ad558ccc94f0b626063d2ae2c0c51
ans_hash_43689	8781f494c0adfece9cca3794530c34b6
ans_hash_43690	e8cd59e337f78df1933e1101db888152
ans_hash_43694	6932d955078d0ce7a0d230b5f474c8f1
ans_hash_43699	643dcddd966a846968d964d0206f637c
ans_hash_43705	e05a023a65f003ec6ad75c1280446915
ans_hash_43706	5fc84e0f587563325331cd96339c2689
ans_hash_43714	b6fd6fe74e2c9df9ebd8e5a30d3b42a1
ans_hash_43716	c6e4ca38b2785f4ecb16fb3bd9b6ef02
ans_hash_43721	4d4c2977a88e9f4240a844c0849ba22e
ans_hash_43724	1a4fd6e537dd6ae2a3254fc189b2042e
ans_hash_43736	87eaafc646e3b4fee6445b1316672651
ans_hash_43737	33f023302c9d8a35afea6d4996cca940
ans_hash_43738	5361fe0001ad7468953bdadb91d5b3d1
ans_hash_43742	f3b64e9e7cb096cb42f0bdbe6c2302b4
ans_hash_43752	1394256e4ba27a6407245443d90ad629
qs_ids	43659,43667,43675,43679,43681,43689,43690,43694,43699,43705,43706,43714,43716,43721,43724,43736,43737,43738,43742,43752

qs_ids=43659%2C43667%2C43675%2C43679%2C43681%2C43689%2C43690%2C43694%2C43699%2C43705%2C43706%2C43714%2C43716%2C43721%2C43724%2C43736%2C43737%2C43738%2C43742%2C43752&ans_hash_43659=811260173aa28331ecf6e2ed3985c86c&ans_hash_43667=15a9ca748fcbc9296df4d4819a5897a4&ans_hash_43675=ce7204c4030211a43da491e8fed414e8&ans_hash_43679=b785d82ab875d9d07673603222cb2bbc&ans_hash_43681=c77ad558ccc94f0b626063d2ae2c0c51&ans_hash_43689=8781f494c0adfece9cca3794530c34b6&ans_hash_43690=e8cd59e337f78df1933e1101db888152&ans_hash_43694=6932d955078d0ce7a0d230b5f474c8f1&ans_hash_43699=643dcddd966a846968d964d0206f637c&ans_hash_43705=e05a023a65f003ec6ad75c1280446915&ans_hash_43706=5fc84e0f587563325331cd96339c2689&ans_hash_43714=b6fd6fe74e2c9df9ebd8e5a30d3b42a1&ans_hash_43716=c6e4ca38b2785f4ecb16fb3bd9b6ef02&ans_hash_43721=4d4c2977a88e9f4240a844c0849ba22e&ans_hash_43724=1a4fd6e537dd6ae2a3254fc189b2042e&ans_hash_43736=87eaafc646e3b4fee6445b1316672651&ans_hash_43737=33f023302c9d8a35afea6d4996cca940&ans_hash_43738=5361fe0001ad7468953bdadb91d5b3d1&ans_hash_43742=f3b64e9e7cb096cb42f0bdbe6c2302b4&ans_hash_43752=1394256e4ba27a6407245443d90ad629


答题2 这个可以回答多次
https://account.bilibili.com/answer/promotion
获得要答的类型
https://account.bilibili.com/answer/getProType

选择3个类型 进行提交
https://account.bilibili.com/answer/getQstByType
post数据 type_ids 11,12,13
返回一个json, 里面是一堆问题, 格式和答题1完全一致
提交答案
https://account.bilibili.com/answer/checkPAns
格式和答题1完全一致
{
"status": true,
"data": "/answer/cool/7211548"
}
然后到https://account.bilibili.com/answer/cool/7211548去看结果
https://account.bilibili.com/answer/cool/7212202

提交答题1
https://account.bilibili.com/answer/goPromotion
post数据
qs_ids:36625,43658,43664,43678,43691,43698,43701,43703,43707,43709,43710,43711,43715,43716,43727,43730,43738,43743,43751,43752
ans_hash_36625:ee125761ed535882c5af48d357fd75a6
ans_hash_43658:8e448f66849f734ab4733c0348e297fd
ans_hash_43664:a1b9131d01ca672e7f33d47317049d10
ans_hash_43678:7432cc1849a67f7f628c7bd0d5e06643
ans_hash_43691:7c79f251d5acc9dc0dd05a0dce9cccd2
ans_hash_43698:b5e2dc15ae44c5e5674d651e0aa87ce3
ans_hash_43701:75949db722fadfc3f37fd47d8fda763d
ans_hash_43703:48dcd3bc89203e0afec4a0ffff88be0f
ans_hash_43707:505aa06250d15909db0387ecf34f0e97
ans_hash_43709:3500a5442c8a3058fc7958191342eb4f
ans_hash_43710:ec892540106e43f66c36329930d7d2f1
ans_hash_43711:5acf3cd7074fdff326a3d5c75370b939
ans_hash_43715:821f6cd4733ebb1d27bc205a866a91b3
ans_hash_43716:0347026f44bc3ab426a214bbd548ad28
ans_hash_43727:176b0d8590e5c3476893eab80adda753
ans_hash_43730:af9484131ab7244cb169e9fdeb3b1a49
ans_hash_43738:01f0e66b37ad0d62a752aa131cb93355
ans_hash_43743:432a9076fe40a11a00d92585215b8181
ans_hash_43751:186e389f768c8043a20a47627f01e35c
ans_hash_43752:18bf7fb552c45f63541518b8d7c984cb



