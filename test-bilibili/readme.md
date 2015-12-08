# API收藏 #
http://www.scriptsuser.org/code.php?hc=206
http://www.fuckbilibili.com/biliapi.html

获得每周的番剧更新
http://www.bilibili.com/index/index-bangumi-timeline.json

获取某个专题的信息
http://api.bilibili.cn/sp?spid=56746

查看某用户的投稿
http://space.bilibili.com/ajax/member/getSubmitVideos?mid=928123&pagesize=30&page=1

# 视频 #
获得视频信息
http://api.bilibili.cn/view?appkey=03fc8eb101b091fb&id=3342515
这个只能获得正常状态的视频信息
其他状态(无权限,已删除,不存在)会有code=404

添加
http://api.bilibili.com/favourite/add?id=3335348

获取用户的收藏夹
这个似乎没有做权限认证, 所以可以获取其他人的收藏夹?
http://space.bilibili.com/ajax/fav/getBoxList?mid=19161363
和[获取收藏的视频]搭配使用可以获取别人的收藏信息!

收藏列表
http://space.bilibili.com/ajax/fav/getList?mid=19216452&pagesize=30&fid=19453437
pagesize最大在70左右

删除收藏的视频
需要post操作, 还需要Refer头
http://space.bilibili.com/ajax/fav/mdel
fid:2424663
aids:3296072
返回值{"status":true}

获得评论列表
http://api.bilibili.com/feedback?page=1&mode=arc&type=json&ver=3&order=default&pagesize=1&aid=2007731

# 用户 #
获取用户信息
两种方式获取的数据基本一致, 推荐第二种
http://space.bilibili.com/ajax/member/GetInfo?mid=19161363
http://api.bilibili.cn/userinfo?user=xuzhichaoxh1
	或mid=?


注册成功
http://www.bilibili.com/account/register_success

礼仪答题
https://account.bilibili.com/answer/base

答题2
https://account.bilibili.com/answer/promotion
获得要答的类型
https://account.bilibili.com/answer/getProType


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