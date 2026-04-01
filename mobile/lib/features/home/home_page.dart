import 'package:flutter/material.dart';

import '../../app/router.dart';
import '../../app/theme/app_colors.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  int selectedNavIndex = 0;

  static const List<_RecentStamp> _recentStamps = [
    _RecentStamp(
      title: 'Central Square',
      time: '2h ago',
      colors: [Color(0xFFDFF4FF), Color(0xFFB7E1FF)],
    ),
    _RecentStamp(
      title: 'Riverside Pk',
      time: 'Yesterday',
      colors: [Color(0xFFF5DDC8), Color(0xFFC8A481)],
    ),
    _RecentStamp(
      title: 'Old Market',
      time: '3d ago',
      colors: [Color(0xFFCBD7E2), Color(0xFF7F95A8)],
    ),
  ];

  static const List<_QuickAction> _quickActions = [
    _QuickAction(
      icon: Icons.map_outlined,
      label: 'Nearby Stations',
      accent: AppColors.brandBlue,
      routeName: AppRouter.stations,
    ),
    _QuickAction(
      icon: Icons.card_giftcard_outlined,
      label: 'Claim Rewards',
      accent: AppColors.brandRed,
      routeName: AppRouter.rewards,
    ),
    _QuickAction(
      icon: Icons.receipt_long_outlined,
      label: 'Line Progress',
      accent: AppColors.brandBlue,
    ),
    _QuickAction(
      icon: Icons.bolt_outlined,
      label: 'Achievements',
      accent: Color(0xFFF59E0B),
    ),
  ];

  static const List<_PartnerBrand> _partnerBrands = [
    _PartnerBrand(
      name: '',
      shortName: 'HC',
      foreground: Color(0xFF8D1F24),
      background: Color(0xFFFFF4EC),
      backgroundUrl:
          'https://i.pinimg.com/1200x/12/d0/d8/12d0d8eb92a2677cd43f4132ecdb7c77.jpg',
      logoUrl:
          'https://i.pinimg.com/736x/9e/d3/65/9ed3653a9eb6cad4d9eef5d1999a1e25.jpg',
    ),
    _PartnerBrand(
      name: '',
      shortName: 'PL',
      foreground: Color(0xFF0E5A36),
      background: Color(0xFFF2FBF5),
      backgroundUrl:
          'https://scontent.fsgn5-15.fna.fbcdn.net/v/t39.30808-6/530093388_763139393030637_5646777646757515893_n.jpg?_nc_cat=103&ccb=1-7&_nc_sid=2a1932&_nc_eui2=AeE_FP8HM_O5HllK8rcf6ZV8A0ET7Mw53y8DQRPszDnfL5llqn82yCCoD-OueM0-N7F-vbYcCQcblMvy41h521DX&_nc_ohc=DuuvdGh8qEYQ7kNvwHJqlgD&_nc_oc=AdovHB5BITjWFHuf8RmBlmBDTtvdkH57Cc3G-8Y5Io4fELHFe0c5Io73Tm7Uu1yEwjWqnkk2kC0vt3xs2s2kgvc3&_nc_zt=23&_nc_ht=scontent.fsgn5-15.fna&_nc_gid=By-TicxH9ECZxR0kxvraBw&_nc_ss=7a32e&oh=00_AfzgguV-vh8CYUO_1SqXpTRFRDe03p-N2dMnJTe6SIJU4Q&oe=69C6DFDA',
      logoUrl:
          'https://scontent.fsgn5-15.fna.fbcdn.net/v/t39.30808-6/530481361_767926022551974_8185202329016233703_n.jpg?_nc_cat=103&ccb=1-7&_nc_sid=1d70fc&_nc_eui2=AeGCQ_i9gg48Egjv2JMj15RhLAyGxe8r18wsDIbF7yvXzKTS8Ul53SqN8apTjpRI6OrNi9omCYpM4XXLmqPhLYDI&_nc_ohc=MbqmFQ9EB5YQ7kNvwE7iuC0&_nc_oc=Ado16F0isx22VbPEox21qr4waVrRC2QdZVcKvI-iD5WCWTPSw-qDDD7bpefqOOqnhTiobHstwDZp6Dmt6-Ozs9wE&_nc_zt=23&_nc_ht=scontent.fsgn5-15.fna&_nc_gid=aCvb-EH7ZIbOdWFTi4UItA&_nc_ss=7a32e&oh=00_AfyfKpJf2oGYupmffWfx-odLrBt-ZrmgPQVOkCUHTwJ4UA&oe=69C6DBD6',
    ),
    _PartnerBrand(
      name: '',
      shortName: 'TCH',
      foreground: Color(0xFFE07A1F),
      background: Color(0xFFFFF6EE),
      backgroundUrl:
          'https://i.pinimg.com/1200x/f4/7d/ba/f47dba17d5e0e2d7d1b9c9b6c7faa37b.jpg',
      logoUrl:
          'https://i.pinimg.com/1200x/b6/7b/c9/b67bc9ebefd61b843310442fdf01ca0a.jpg',
    ),
    _PartnerBrand(
      name: '',
      shortName: 'KT',
      foreground: Color(0xFF2F5D9F),
      background: Color(0xFFF1F6FF),
      backgroundUrl:
          'https://i.pinimg.com/1200x/49/3b/67/493b6798262c09a08f9730e749ef0b1a.jpg',
      logoUrl:
          'https://i.pinimg.com/1200x/eb/ee/81/ebee81af69e496b007b267c5723b6377.jpg',
    ),
    _PartnerBrand(
      name: '',
      shortName: 'SB',
      foreground: Color(0xFF006241),
      background: Color(0xFFF1FBF7),
      backgroundUrl:
          'https://i.pinimg.com/1200x/bc/2f/82/bc2f82cc1f944aa7a617364938a2c496.jpg',
      logoUrl:
          'https://i.pinimg.com/1200x/59/83/14/598314074a9a7f3fcd0facf8f27d744d.jpg',
    ),
    _PartnerBrand(
      name: 'Trung Nguyen Legend',
      shortName: 'TNL',
      foreground: Color(0xFF5A331C),
      background: Color(0xFFFFF3EA),
      backgroundUrl:
          'https://scontent.fsgn5-15.fna.fbcdn.net/v/t39.30808-6/654891412_1242089191412424_5085845350756720205_n.png?_nc_cat=102&ccb=1-7&_nc_sid=2a1932&_nc_eui2=AeHpOP5WT7uyYOoNKwU2dsT8xEfmTRdclAPER-ZNF1yUA6vdcG8NS09e5htA05Ms3eQM0Sa7MijZ_MOM89o1PELb&_nc_ohc=wTd-1pjs4bcQ7kNvwG9BgJU&_nc_oc=Ado9vUVM2h61Gca54wmHZO6ZCiwgvBkvqvrQiUcXbgWxlekLtispihrcj3RDge8yhcxyDslPbCuvHvNM_kh5rR5o&_nc_zt=23&_nc_ht=scontent.fsgn5-15.fna&_nc_gid=mHoZxL1VXmDlXtcWWUGZ_A&_nc_ss=7a32e&oh=00_AfxWpvsAKHVASWlvsbO-OfGqof291tSAeMioeSpsfrQ7pA&oe=69C6D05D',
      logoUrl:
          'https://scontent.fsgn5-15.fna.fbcdn.net/v/t39.30808-6/347560257_763197765508097_5202542560020711664_n.png?_nc_cat=108&ccb=1-7&_nc_sid=1d70fc&_nc_eui2=AeF1Rek0RGB0de796CGb_uWuieSv_4TjyY2J5K__hOPJjZ_JpGWceOUa-KDT3D3EhX56LORi3NHcj5vRVx43rILv&_nc_ohc=X476g8Y_QOoQ7kNvwFpfAwF&_nc_oc=Adp1w7CUg7mSwgZS1NcsUI2Ll2w-frCKKLryBj0V8DObDt_G9Vl8i_BFtr0pN9vXj2e55nZlWDSuNGgrRTTLsDkx&_nc_zt=23&_nc_ht=scontent.fsgn5-15.fna&_nc_gid=acYkyWX56gIfTFEdqMCLLw&_nc_ss=7a32e&oh=00_Afy0hqkUKf5e-rAuObykWPk3GInwVvntlm-xCPoAgyZRbg&oe=69C6F3E5',
    ),
    _PartnerBrand(
      name: 'KOI The',
      shortName: 'KOI',
      foreground: Color(0xFFC49A2C),
      background: Color(0xFFFFF9E8),
      backgroundUrl:
          'https://scontent.fsgn5-14.fna.fbcdn.net/v/t39.30808-6/644035322_1326121166217810_467634450496160415_n.jpg?_nc_cat=104&ccb=1-7&_nc_sid=2a1932&_nc_eui2=AeEzAv0Fm039IlSwZn8JRFp0V3Bx3cg71mdXcHHdyDvWZ5QFfgTrkrXFb9JIjlLmk-TRbSl7ajzmpYXVqhhn8959&_nc_ohc=tfYadWTQ3mUQ7kNvwF0tJMo&_nc_oc=Adqx3lB_DqFMxmHT3ot2ve041BYymSvKY9t78aM_USjPEMCITLrtX14srvBuJmpwdPJwY5CgNwt6bcTeS-SASbny&_nc_zt=23&_nc_ht=scontent.fsgn5-14.fna&_nc_gid=X4IZPQqJ1Bpq7aN2_fGo-g&_nc_ss=7a32e&oh=00_AfyJJejV1GzeGS6d_stIrRTY1Kvpu788r1jwyTLkkV-mow&oe=69C6D3C0',
      logoUrl:
          'https://scontent.fsgn5-14.fna.fbcdn.net/v/t39.30808-6/645595923_1326120322884561_3513264020654438089_n.jpg?_nc_cat=1&ccb=1-7&_nc_sid=1d70fc&_nc_eui2=AeEwQLFo4ZjikG0hwm-N3wiAsJjCBgsgeM6wmMIGCyB4zsJMY7NtkVFvk3Xqs5vUX0vtJvSkDqXigk77J1XDmyAt&_nc_ohc=Q6yNBCKQ0Q0Q7kNvwGr96Vw&_nc_oc=AdrW4DzaZNKzinYpEKWJhlQcku2TlH4oFgmOBi59Q9RNxv_VnR3kY6Rj3yv2DE2nvzcPPMMNzEDqK_mo_D5nH720&_nc_zt=23&_nc_ht=scontent.fsgn5-14.fna&_nc_gid=rUzG-GHoYp9Zhk_n1a089A&_nc_ss=7a32e&oh=00_AfyANIuCQz__PnDohYi6vhy_hXsO1m6_yfj3YQyCK8ZTZw&oe=69C6DBBA',
    ),
    _PartnerBrand(
      name: 'Tocotoco',
      shortName: 'TT',
      foreground: Color(0xFFC47A17),
      background: Color(0xFFFFF5E8),
      backgroundUrl:
          'https://i.pinimg.com/736x/8f/aa/49/8faa49926010abc49aa7dbde80be03b7.jpg',
      logoUrl:
          'https://scontent.fsgn5-15.fna.fbcdn.net/v/t39.30808-6/479485690_1144477067685177_6509851446432183740_n.jpg?_nc_cat=102&ccb=1-7&_nc_sid=1d70fc&_nc_eui2=AeHxn7d0FDroply01GK1tM_Dg2lkLj3uNr-DaWQuPe42v2rQyxwJnvyysWWbfnmaSg7mhCb10BbkiLfLK4kDhZdY&_nc_ohc=8MjZFN0xRRgQ7kNvwFgO8lj&_nc_oc=Adow6NV8nFcikeMuZKTkTYs7XSNyQ6NWk_Nci76TKrrtrc7Q6glnPl8BowpjWug2_CXi70hqaRi3DHJM0WxDdpwt&_nc_zt=23&_nc_ht=scontent.fsgn5-15.fna&_nc_gid=XpzA6xHUoVp_O6CNXhc8fA&_nc_ss=7a32e&oh=00_AfwsXh-X9o2DVxCAsCzkqNtREnrEaXY5LO9PlhXnkLQwWA&oe=69C6FAAD',
    ),
    _PartnerBrand(
      name: 'Gong Cha',
      shortName: 'GC',
      foreground: Color(0xFFA61E2C),
      background: Color(0xFFFFF1F2),
      backgroundUrl:
          'https://scontent.fsgn5-15.fna.fbcdn.net/v/t39.30808-6/642405764_1218762103806446_8525129192315707292_n.jpg?_nc_cat=107&ccb=1-7&_nc_sid=2a1932&_nc_eui2=AeFHcERLTW1gV5hf2b8EGWZu-A5XWbZaU0f4DldZtlpTR4ifJ7PsduD0q3Cisaaxx0gjVewOOWb54Bw8JIXAX1MU&_nc_ohc=KlGwe_oDM9wQ7kNvwGWH7Yq&_nc_oc=Adot-2d03pS9ITJl7dcpziwaNoHSrpSil3-Kf7LudGZ5jRIjLcBeddU71940MV3uWeTpEmCnFmZLZguxHtPYZGYk&_nc_zt=23&_nc_ht=scontent.fsgn5-15.fna&_nc_gid=99MO3GG1BwEhVlvW3OboTg&_nc_ss=7a32e&oh=00_AfwNn55YtxmPN96BzE7rogCGNu-C93BH6fk5L8Lcnjmg_A&oe=69C6E761',
      logoUrl:
          'https://scontent.fsgn5-14.fna.fbcdn.net/v/t39.30808-6/644444799_1218817083800948_8250065170366988871_n.jpg?_nc_cat=1&ccb=1-7&_nc_sid=1d70fc&_nc_eui2=AeGCr9wS2mfNiWNZX7ggahiC2Ay7kmtHZxrYDLuSa0dnGtC-UKSupfFJRvcOorfmu8esB1STNYO2gdxEubpNRn4G&_nc_ohc=kRBBkwIEvAYQ7kNvwEm3KzW&_nc_oc=AdoI8jW_Xs8RrO5_ZDCeTJ7gD904H5YPB6lGNcKHzOByQ9lEaxcPPbtW28B9he4u_bigRIauf3HL7-z7WK_g2llp&_nc_zt=23&_nc_ht=scontent.fsgn5-14.fna&_nc_gid=s9IquRsnq2xPj_gnZG25xw&_nc_ss=7a32e&oh=00_Afyu5a7lpI-brgeQyd2A7Dyah5mwpR-QlYc248WgR-0-2w&oe=69C6E2FB',
    ),
    _PartnerBrand(
      name: 'Pizza 4P\'s',
      shortName: '4P',
      foreground: Color(0xFF275D9A),
      background: Color(0xFFF0F7FF),
      backgroundUrl:
          'https://scontent.fsgn5-14.fna.fbcdn.net/v/t39.30808-6/632163276_1333262748844474_5167113143857901713_n.jpg?_nc_cat=109&ccb=1-7&_nc_sid=2a1932&_nc_eui2=AeGBWI-7sJSyBg1NLtEvrBYLngnSzPAotjWeCdLM8Ci2NUhqxeYXOAs0WlSfQtQ7FDRpAI3qW9nRGYjP7kxb7hBD&_nc_ohc=-RRO5FYmnyAQ7kNvwHUdlzS&_nc_oc=Adq-_VqtqJ2ZUYdilTNg_weqbsYKLQPgKIoXxdnO7mVfT2X4V6yF2ixUqEFVqijzlYNC2Wa-jhyUC_Q-37kz_t3i&_nc_zt=23&_nc_ht=scontent.fsgn5-14.fna&_nc_gid=5spHAzom8rweHQFnbiUBfw&_nc_ss=7a32e&oh=00_Afy2imh_j5Op0WUBXAoQ6yfKMCGzxuaHTt1biUveBrrCQA&oe=69C6F4ED',
      logoUrl:
          'https://scontent.fsgn5-14.fna.fbcdn.net/v/t39.30808-6/352386659_630892538962734_2991141170219283993_n.jpg?_nc_cat=111&ccb=1-7&_nc_sid=1d70fc&_nc_eui2=AeEtEDrD0xf2lsA2t0eV6AlVIrgBNOrCYSciuAE06sJhJ0Yka7Y3UHoRAkTjBMOn_-YU7ANWi5iSV5RDUzGsseq4&_nc_ohc=rc1zk3Z5MPsQ7kNvwE-jwDB&_nc_oc=Adpanh8ByTtfupAamSTAOsoPZay7LhrVRLv9E-XTIhwVyGRfX-IABTy7FGe1P1-p0j10HjjtrmH65N2v0EAz2iIp&_nc_zt=23&_nc_ht=scontent.fsgn5-14.fna&_nc_gid=T0Zya36AqzpG0-YKtid0hg&_nc_ss=7a32e&oh=00_Afwa4BdBcMAkTCHvMOMwBN0tINgNIQ0oCh1J6JS7ofSwOQ&oe=69C6DE00',
    ),
    _PartnerBrand(
      name: 'McDonald\'s',
      shortName: 'M',
      foreground: Color(0xFFC62828),
      background: Color(0xFFFFF1D6),
      backgroundUrl:
          'https://scontent.fsgn5-14.fna.fbcdn.net/v/t39.30808-6/641307254_1352835623550471_1603724107372399733_n.png?_nc_cat=106&ccb=1-7&_nc_sid=2a1932&_nc_eui2=AeHq_QPX2CnIxKZ3e54tpVG-rUhYVs54I4GtSFhWzngjgft5Cf12ep70VVQ5WEqXaRgugMqzW8NkeSl64_6TrIOp&_nc_ohc=oDUNGkPjFeUQ7kNvwFxWpU7&_nc_oc=AdrVK1jCEWkyVCXBJ2D2CKEvyhvUMofp0fJS8sRZZBBuLa3GjTXZLAhlXA-R4l8ZtLOZon0i9yPFymPXIZMVetav&_nc_zt=23&_nc_ht=scontent.fsgn5-14.fna&_nc_gid=QFZd9Q6cpXIbIbh7mR3-MA&_nc_ss=7a32e&oh=00_AfyUlO-oNXQJHRN83Sv-o-ta-1FKCqBmXkkdpUH0peK4AA&oe=69C6EABD',
      logoUrl:
          'https://scontent.fsgn5-14.fna.fbcdn.net/v/t39.30808-6/497537385_1117948297039206_4126351640918833323_n.jpg?_nc_cat=1&ccb=1-7&_nc_sid=1d70fc&_nc_eui2=AeHDyTINt-hb6eIzhdL5oWO9LBPxhHkoGwQsE_GEeSgbBG9QBiDGW1VxHa1906M2BG--yYO4jgH6KWuFfAJo_DAJ&_nc_ohc=3ej8Tx34EO4Q7kNvwGV5vpH&_nc_oc=AdqhKgrOnTQFjnCIDE8MN7puxl6_Upy0Zxwd4VEYljkxO9V1Js_TwM9w6FXud1VL5lxNs-svRIoBjL0uKM6pTpWs&_nc_zt=23&_nc_ht=scontent.fsgn5-14.fna&_nc_gid=A-8vtUpGrR6tZSY2Q6dYhA&_nc_ss=7a32e&oh=00_AfwGRC4sevhISgbsqS6dL67h2whYoxYARNXIwnzr03_1Sw&oe=69C6D5CA',
    ),
    _PartnerBrand(
      name: 'KFC',
      shortName: 'K',
      foreground: Color(0xFFA32638),
      background: Color(0xFFFFF4F6),
      backgroundUrl:
          'https://scontent.fsgn5-15.fna.fbcdn.net/v/t39.30808-6/658400591_1360604282759696_6894372760387414646_n.jpg?_nc_cat=108&ccb=1-7&_nc_sid=2a1932&_nc_eui2=AeGmFss4R11jKY8ZGeZmOPRDRMmi5m4a6LxEyaLmbhrovAnNygx1YyK_ZsfrNKbkq_gqizZJpYx43tfVPgLtP8vO&_nc_ohc=c6UrVBpVaRsQ7kNvwFM2ous&_nc_oc=Ado7BLB4Y02F-OY-RG7--53QYLp_vd7lGXVNm0T0OrQXCi9pWC9ZMgd_n7lEyqaHBDWLYZj4lG10inpuOqxXk842&_nc_zt=23&_nc_ht=scontent.fsgn5-15.fna&_nc_gid=Gk2E6JXAS8znSMeOv0IHmg&_nc_ss=7a32e&oh=00_Afxq7L8Rp1Kiq0TssOd0WqyKtR1AWlXUXxLBUpK260SEXg&oe=69C6DA79',
      logoUrl:
          'https://scontent.fsgn5-14.fna.fbcdn.net/v/t39.30808-6/453344341_893890756097720_3035118305755741882_n.jpg?_nc_cat=1&ccb=1-7&_nc_sid=1d70fc&_nc_eui2=AeGfsMyolSABNyYKdpX7JW1fVfifUhX2Sn9V-J9SFfZKf6kfaFmqkqJrtNPNir9P4UtIchjMIDm-BSRUwoVJGk8C&_nc_ohc=WqEXb2j_SJcQ7kNvwFu-zzH&_nc_oc=AdrSTZFyp1BH1JtIi-h7e1PuQHXUgmRA7EzdambF7YQI8WWeHKHY4H8-YnziSl2ec8cEWuzLbW-C8vFjZwtzKPeC&_nc_zt=23&_nc_ht=scontent.fsgn5-14.fna&_nc_gid=6dq8cEBPVXTRtcBOl5PXhw&_nc_ss=7a32e&oh=00_Afwa-UC4hgDG0kY_uphcfq2dfBUv_4flCu5EDew03Dji4A&oe=69C700AC',
    ),
    _PartnerBrand(
      name: 'Lotteria',
      shortName: 'L',
      foreground: Color(0xFFDF1F37),
      background: Color(0xFFFFF0F3),
      backgroundUrl:
          'https://scontent.fsgn5-15.fna.fbcdn.net/v/t39.30808-6/654177658_1354287133395278_7413674825706912639_n.jpg?_nc_cat=108&ccb=1-7&_nc_sid=2a1932&_nc_eui2=AeEMw6uR7ugS_wY0bagFXBjd3nN6EU2ozTvec3oRTajNO6Uthqs5iwq23QzHOOjI17bv5MKJWBIdYNCn2-2AMJbc&_nc_ohc=JK6ZVQm_7h0Q7kNvwEFu-4T&_nc_oc=AdprddGsP2L50hnqzjbnMit-lPEv05dPZuIofKmF0xfoOI89Nfmmvo2nSDyia5yrEPhpHs1u4ZzwLQWGMfVfZUcr&_nc_zt=23&_nc_ht=scontent.fsgn5-15.fna&_nc_gid=t4Yi96eMDXRMpGI2HGMi3A&_nc_ss=7a32e&oh=00_AfxWxzOjmlz8JGE5OYKg6jqY6PZbDnWYzuk2jmc4YtpRwA&oe=69C6EA0D',
      logoUrl:
          'https://scontent.fsgn5-14.fna.fbcdn.net/v/t39.30808-6/526929535_1166918508798809_985463433921479655_n.jpg?_nc_cat=111&ccb=1-7&_nc_sid=1d70fc&_nc_eui2=AeGgwlXIOmqg8tVFgCrRhJwtA015JKdz1-8DTXkkp3PX7xZFSZvDvBXJYamMGbdyGc-muCzagfs-I20DNAMI4mg2&_nc_ohc=wA-g-0T4l6sQ7kNvwG5PJKV&_nc_oc=AdqM-oqXYlDEY7mPRNc2RpEJtkBQiF0GsKrhMKlQd2I2R25Ia46KHkYhnU2ofw5S-qwqBJ1hCLsgFcXGWVbpq_hx&_nc_zt=23&_nc_ht=scontent.fsgn5-14.fna&_nc_gid=Gyxjdr6AOzmBA6OSu7UHng&_nc_ss=7a32e&oh=00_AfwFm4tesfnw6fst6KqimJp0bJ3GUhWLE0M7KWMaXfiw9g&oe=69C6CE67',
    ),
    _PartnerBrand(
      name: 'Jollibee',
      shortName: 'J',
      foreground: Color(0xFFE53935),
      background: Color(0xFFFFF0EE),
      backgroundUrl:
          'https://scontent.fsgn5-15.fna.fbcdn.net/v/t39.30808-6/652321562_1378694397623948_2968276718511574706_n.jpg?_nc_cat=100&ccb=1-7&_nc_sid=2a1932&_nc_eui2=AeFFDb-6DnVUBJrU6MLqFYgNauMROidXxC9q4xE6J1fEL-cIB0BUyYNAZK5TUUXnGpeMhJ_lhr-_kx80AKtecyZy&_nc_ohc=i9_c-1D9EOkQ7kNvwEN_ESI&_nc_oc=AdodOixUDbUSAMRap94polKNHrixmuWnzjptuqr9WYfdlv1Fun_deHHz2IldIYTIZnA6ESq9aBjGtWcPwNTKIXkt&_nc_zt=23&_nc_ht=scontent.fsgn5-15.fna&_nc_gid=ZuJ2goVsAD54x5r4LhLDeg&_nc_ss=7a32e&oh=00_AfyhJegkx0nlZ1Iys4AXECw1lsP9C2EvxYIOqoIFS6z1Sw&oe=69C6F772',
      logoUrl:
          'https://scontent.fsgn5-14.fna.fbcdn.net/v/t39.30808-6/351501597_911814079905689_5093210085194296086_n.jpg?_nc_cat=1&ccb=1-7&_nc_sid=1d70fc&_nc_eui2=AeEVFW6UMmSoLaR-9peo9ascZcIwc147F2hlwjBzXjsXaJAPUOyZPYPM5OISraGb7yGwbLUV7RkBkEnR0qDqw1is&_nc_ohc=_rVttX6vvI0Q7kNvwF9hXMM&_nc_oc=AdqDR3OzyNFi7LeOoyH7rry2-YxgG55lv54ieZQS4wc6zlWDuoi7n3y5MO6SqpVx5UB0zLIHvFRow9-QDjNH-AZ_&_nc_zt=23&_nc_ht=scontent.fsgn5-14.fna&_nc_gid=N6eNYGeqkNg9Adis0SpZOQ&_nc_ss=7a32e&oh=00_AfwLHzx4CjST4l4PQWFFB-1YJn5PS0WmAOsfynM-WstBPQ&oe=69C6E00C',
    ),
    _PartnerBrand(
      name: 'CGV Cinemas',
      shortName: 'CGV',
      foreground: Color(0xFFE53935),
      background: Color(0xFFFFF1F0),
      backgroundUrl:
          'https://scontent.fsgn5-14.fna.fbcdn.net/v/t39.30808-6/655299955_1436504705182962_8141214812700810478_n.png?_nc_cat=106&ccb=1-7&_nc_sid=2a1932&_nc_eui2=AeFd3RaSoQz891bgiIGZ4WgsXrxyPqAiF4hevHI-oCIXiHZ2HKluUBD9MbRPrTiBEZSPBZS_ll6EkAzqweMyhx_d&_nc_ohc=Gg1hammJcHYQ7kNvwEczUzP&_nc_oc=AdrJxE7gBXpoXyllHOrLxMW4zvkJdez0FYD8C0R00d2AeXvFvo-QF7fTVHDoo3KOqk0e7-MSJ7h9jwbRqTteyvsz&_nc_zt=23&_nc_ht=scontent.fsgn5-14.fna&_nc_gid=62nWW8xa4lV9gb03IiFKpg&_nc_ss=7a32e&oh=00_Afz281b5Sp5wYk_IqKMJKq0TT0uzSKjNp5idAaIlknYzKQ&oe=69C6FA24',
      logoUrl:
          'https://scontent.fsgn5-14.fna.fbcdn.net/v/t39.30808-6/646810750_1423371523162947_2893416629564194439_n.jpg?_nc_cat=1&ccb=1-7&_nc_sid=1d70fc&_nc_eui2=AeGGlRp0ffasrfCCd3EXiBlLC8SBRdrWwXoLxIFF2tbBeodaKt5zFF67qEKmeN0TYNIGwDRKlhKkxmhxrp4OZ9LH&_nc_ohc=orLK6aJ41bAQ7kNvwHs_RQQ&_nc_oc=AdrUUtxukicgbhhnkWEA04OGDTNSI-UtnGVS9ktkROFmOQ6Hvt2DJJidNWO3mu03wU-evhqmPgxBVxoJJhKHjUCX&_nc_zt=23&_nc_ht=scontent.fsgn5-14.fna&_nc_gid=8yGDKT9M7m5S0SZfeWB8qQ&_nc_ss=7a32e&oh=00_AfxeajvSbRcYAhMQMCi-yb6K0wwjoy6GuaZgsexuTip0Bw&oe=69C6CF0D',
    ),
    _PartnerBrand(
      name: 'Circle K',
      shortName: 'CK',
      foreground: Color(0xFFCB1F2F),
      background: Color(0xFFFFF2F3),
      backgroundUrl:
          'https://scontent.fsgn5-15.fna.fbcdn.net/v/t39.30808-6/482960087_969967311915164_1567644152378862626_n.jpg?_nc_cat=105&ccb=1-7&_nc_sid=2a1932&_nc_eui2=AeHD7CjZTClk7DRpDNhly4ZXlbcUCJlpV5eVtxQImWlXlwjcJuB9HBkvJ1A1EqV-MkfrdsRwgwVkrB-yDAYbFVz8&_nc_ohc=KwW3AdZIInYQ7kNvwG9nxdJ&_nc_oc=AdpnMxghLogPrTuxwnnTb8GsxuR-krzD6LZG7NMFF1newF2Vl2bItgSFCC3iDi0XJUbfxJ_sYI6-hwQYXbtZuikr&_nc_zt=23&_nc_ht=scontent.fsgn5-15.fna&_nc_gid=Bd57O4K4fizUv--c5RFj6A&_nc_ss=7a32e&oh=00_AfwNGtsUU7MkjYgnfnrjFtIv3T-TuMn12L8bpHL-JlaVpQ&oe=69C6E7ED',
      logoUrl:
          'https://scontent.fsgn5-14.fna.fbcdn.net/v/t39.30808-6/352989920_582743623970870_9138107434726396156_n.png?_nc_cat=111&ccb=1-7&_nc_sid=1d70fc&_nc_eui2=AeF67J4EOqjLwAlLMIH_jwEzRUvRTMv7mxZFS9FMy_ubFplZ5qnBKTjEa7vbmdlyc3j3zj-87Nk1e5T-42e8m3b1&_nc_ohc=_SO0JXRv6j0Q7kNvwEdMwVc&_nc_oc=Adpa7tvkBKYcR8WQIBOP7aNwLKDnuHnIEwwmQqwzVIcjiNfSVR1SMTjKVpGXKnkmtVILnkb3cs6v5ZjN6oCYHGDC&_nc_zt=23&_nc_ht=scontent.fsgn5-14.fna&_nc_gid=Mfop8Fs7DO9SJW7doDbkwg&_nc_ss=7a32e&oh=00_AfyxwNSW9d1iiDXPHJvr1KHhbTkHVPmMQxosZOMU_Y4B5A&oe=69C6E387',
    ),
    _PartnerBrand(
      name: 'WinMart+',
      shortName: 'WM',
      foreground: Color(0xFFB61B2A),
      background: Color(0xFFFFF3F3),
      backgroundUrl:
          'https://scontent.fsgn5-14.fna.fbcdn.net/v/t39.30808-6/652979919_1255205073409856_3718054140484258820_n.jpg?_nc_cat=104&ccb=1-7&_nc_sid=2a1932&_nc_eui2=AeGt4PRbGX4lPGb7HTVlZmbIf41oW4CD58p_jWhbgIPnynJFk7ZzbYQNxRsKJXlHs-UW3kKK7vquD4StRhs_N5Mf&_nc_ohc=TpQv5FFZGzgQ7kNvwHuYhjV&_nc_oc=AdpDaHLWeFgh9if7h1ixwj0bVRe7rsNnsIuWg1PQ_vmpbwlpJNN6pENO2fhSfiI6vhbnJYjVRPo1bkEautdcFiJK&_nc_zt=23&_nc_ht=scontent.fsgn5-14.fna&_nc_gid=cxl6rxQL0gU15sMmlfI6XA&_nc_ss=7a32e&oh=00_AfzxyBUFQhsIHIFdd38wy9nv2l_IXSBiITkzASkAJmJq8A&oe=69C702BA',
      logoUrl:
          'https://scontent.fsgn5-14.fna.fbcdn.net/v/t39.30808-6/504863449_1032121205718245_848324105232452870_n.jpg?_nc_cat=1&ccb=1-7&_nc_sid=1d70fc&_nc_eui2=AeGtmhmBFEp4n3QvsWHcGESteKDL_9myM0l4oMv_2bIzSdr_4WbgxSafnA7dE9cCIhhUMjXoK-1-iHyfsv8d0m5C&_nc_ohc=hQcEpkmMCqsQ7kNvwFzG_da&_nc_oc=AdqX4UedTF0bXFZ3Vpcb_x-PDndHegT6cS_7QVzE7P2-J3B7saBZZ1r86XHE8BhndtlaU28cEpBqQvGEB0acpHq5&_nc_zt=23&_nc_ht=scontent.fsgn5-14.fna&_nc_gid=AOGtIsuLPLSPVOP1jHTbuA&_nc_ss=7a32e&oh=00_AfxE64_A8Ffh_tUbIxFOlaUw9G_B4sYy8kUZtbhG7-l-Zw&oe=69C6EC4C',
    ),
  ];

  static const List<_VoucherCategory> _voucherCategories = [
    _VoucherCategory(
      label: 'All',
      isActive: true,
    ),
    _VoucherCategory(label: 'Coffee'),
    _VoucherCategory(label: 'Dining'),
    _VoucherCategory(label: 'Lifestyle'),
    _VoucherCategory(label: 'Travel'),
  ];

  static const List<_VoucherDeal> _voucherDeals = [
    _VoucherDeal(
      title: 'Highlands Morning Deal',
      subtitle: 'HCM',
      rating: 4.8,
      priceLabel: '50%',
      unitLabel: 'OFF',
      meta: ['2 drinks', '1 pastry', 'Wifi'],
      colors: [Color(0xFFD9EEFF), Color(0xFF6AB3F0)],
      brandLabel: 'HC',
      imageUrl:
          'https://i.pinimg.com/736x/2b/f6/bd/2bf6bdaba3e49720bea75a55d05dba3b.jpg',
      isFavorite: false,
    ),
    _VoucherDeal(
      title: 'Phuc Long Combo',
      subtitle: 'HCM',
      rating: 5.0,
      priceLabel: '30%',
      unitLabel: 'OFF',
      meta: ['Tea set', '3 items', 'Wifi'],
      colors: [Color(0xFFE8DFC9), Color(0xFF8FA364)],
      brandLabel: 'PL',
      imageUrl:
          'https://scontent.fsgn5-14.fna.fbcdn.net/v/t39.30808-6/611970214_3296126127213931_5113625167882351524_n.jpg?_nc_cat=109&ccb=1-7&_nc_sid=e06c5d&_nc_eui2=AeG0BvZAqcsGnY98_FFllmevEYAllsulCEcRgCWWy6UIRykA4ZPb1947S6AZnG_QJCMxzSQvwcU98zD8sXQsnr0Q&_nc_ohc=mV1mviyhgBQQ7kNvwFT3rTH&_nc_oc=AdptzQw2LGJS9jCu8r8AkoLWNYXmX3zUGltlO5gbD6jPaYJ6ln26O5qaeIZbmA6F4zV0u0Kk6fGon2zPHgiXeKMZ&_nc_zt=23&_nc_ht=scontent.fsgn5-14.fna&_nc_gid=X56S6UI1ls-3EDLkn2LRnQ&_nc_ss=7a32e&oh=00_AfxCPut4S6vtY1jNhDsOa5BpPU1VwdD5iUNX5MBpT04FrA&oe=69C6EA37',
      isFavorite: true,
    ),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFFDFDFE),
      floatingActionButtonLocation: FloatingActionButtonLocation.centerDocked,
      floatingActionButton: Container(
        width: 68,
        height: 68,
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          boxShadow: [
            BoxShadow(
              color: AppColors.brandRed.withValues(alpha: 0.28),
              blurRadius: 18,
              offset: const Offset(0, 10),
            ),
          ],
        ),
        child: FloatingActionButton(
          elevation: 0,
          backgroundColor: AppColors.brandRed,
          onPressed: () {
            Navigator.of(context).pushNamed(AppRouter.scan);
          },
          shape: const CircleBorder(),
          child: Container(
            width: 54,
            height: 54,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              border: Border.all(
                color: AppColors.background.withValues(alpha: 0.25),
                width: 1.4,
              ),
            ),
            child: const Icon(Icons.center_focus_strong_rounded, size: 30),
          ),
        ),
      ),
      bottomNavigationBar: _HomeBottomBar(
        selectedIndex: selectedNavIndex,
        onSelected: (index) {
          if (index == selectedNavIndex) {
            return;
          }

          setState(() {
            selectedNavIndex = index;
          });

          switch (index) {
            case 0:
              break;
            case 1:
              Navigator.of(context).pushNamed(AppRouter.stampBook);
              break;
            case 2:
              Navigator.of(context).pushNamed(AppRouter.stations);
              break;
            case 3:
              Navigator.of(context).pushNamed(AppRouter.profile);
              break;
          }
        },
      ),
      body: SafeArea(
        child: CustomScrollView(
          slivers: [
            SliverToBoxAdapter(
              child: Padding(
                padding: const EdgeInsets.fromLTRB(18, 12, 18, 24),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const _HomeTopBar(),
                    const SizedBox(height: 22),
                    const _HeroBanner(),
                    const SizedBox(height: 12),
                    const _HeroDots(),
                    const SizedBox(height: 22),
                    const _ProgressCard(),
                    const SizedBox(height: 26),
                    _SectionHeader(
                      title: 'Recently Collected',
                      trailing: TextButton(
                        onPressed: () {
                          Navigator.of(context).pushNamed(AppRouter.stampBook);
                        },
                        style: TextButton.styleFrom(
                          padding: EdgeInsets.zero,
                          minimumSize: Size.zero,
                          tapTargetSize: MaterialTapTargetSize.shrinkWrap,
                        ),
                        child: const Text('View Book'),
                      ),
                    ),
                    const SizedBox(height: 14),
                    SizedBox(
                      height: 142,
                      child: ListView.separated(
                        scrollDirection: Axis.horizontal,
                        itemCount: _recentStamps.length,
                        separatorBuilder: (context, index) =>
                            const SizedBox(width: 14),
                        itemBuilder: (context, index) {
                          return _RecentStampCard(stamp: _recentStamps[index]);
                        },
                      ),
                    ),
                    const SizedBox(height: 28),
                    const _ScanNowCard(),
                    const SizedBox(height: 18),
                    GridView.builder(
                      shrinkWrap: true,
                      physics: const NeverScrollableScrollPhysics(),
                      itemCount: _quickActions.length,
                      gridDelegate:
                          const SliverGridDelegateWithFixedCrossAxisCount(
                        crossAxisCount: 2,
                        crossAxisSpacing: 14,
                        mainAxisSpacing: 14,
                        childAspectRatio: 1.62,
                      ),
                      itemBuilder: (context, index) {
                        return _QuickActionCard(action: _quickActions[index]);
                      },
                    ),
                    const SizedBox(height: 24),
                    const _SectionHeader(
                      title: 'Đối tác chiến lược',
                      icon: Icons.handshake_outlined,
                    ),
                    const SizedBox(height: 14),
                    const _PartnerMarquee(brands: _partnerBrands),
                    const SizedBox(height: 24),
                    const _VoucherSection(
                      categories: _voucherCategories,
                      deals: _voucherDeals,
                    ),
                    const SizedBox(height: 18),
                    const _CommunityCard(),
                    const SizedBox(height: 28),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _HomeTopBar extends StatelessWidget {
  const _HomeTopBar();

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Container(
          width: 34,
          height: 34,
          decoration: BoxDecoration(
            color: AppColors.brandBlue,
            borderRadius: BorderRadius.circular(9),
          ),
          child: const Icon(
            Icons.workspace_premium_outlined,
            color: AppColors.background,
            size: 20,
          ),
        ),
        const SizedBox(width: 12),
        Expanded(
          child: RichText(
            text: const TextSpan(
              style: TextStyle(
                fontSize: 27,
                fontWeight: FontWeight.w800,
              ),
              children: [
                TextSpan(
                  text: 'Exotic ',
                  style: TextStyle(color: AppColors.brandRed),
                ),
                TextSpan(
                  text: 'Home',
                  style: TextStyle(color: AppColors.brandBlue),
                ),
              ],
            ),
          ),
        ),
        IconButton(
          onPressed: () {
            Navigator.of(context).pushNamed(AppRouter.settings);
          },
          style: IconButton.styleFrom(
            side: const BorderSide(color: AppColors.border),
            backgroundColor: AppColors.background,
          ),
          icon: const Icon(
            Icons.settings_outlined,
            color: AppColors.textPrimary,
          ),
        ),
      ],
    );
  }
}

class _HeroBanner extends StatelessWidget {
  const _HeroBanner();

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 148,
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(22),
        gradient: const LinearGradient(
          colors: [Color(0xFF1D95FF), Color(0xFFB5D9F7)],
          begin: Alignment.centerLeft,
          end: Alignment.centerRight,
        ),
        boxShadow: const [
          BoxShadow(
            color: Color(0x1A09599E),
            blurRadius: 18,
            offset: Offset(0, 10),
          ),
        ],
      ),
      child: Stack(
        children: [
          Positioned(
            top: -14,
            left: -10,
            child: Container(
              width: 104,
              height: 104,
              decoration: BoxDecoration(
                color: AppColors.background.withValues(alpha: 0.10),
                shape: BoxShape.circle,
              ),
            ),
          ),
          Positioned(
            right: -8,
            bottom: -10,
            child: Transform.rotate(
              angle: -0.14,
              child: Container(
                width: 104,
                height: 58,
                decoration: BoxDecoration(
                  color: const Color(0xFF1E2A3B).withValues(alpha: 0.28),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(
                    color: AppColors.background.withValues(alpha: 0.35),
                  ),
                ),
              ),
            ),
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                padding:
                    const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
                decoration: BoxDecoration(
                  color: AppColors.brandRed,
                  borderRadius: BorderRadius.circular(999),
                ),
                child: const Text(
                  'LIMITED OFFER',
                  style: TextStyle(
                    fontSize: 10,
                    fontWeight: FontWeight.w800,
                    color: AppColors.background,
                    letterSpacing: 0.3,
                  ),
                ),
              ),
              const Spacer(),
              const Text(
                'Morning\nBrew Reward',
                style: TextStyle(
                  fontSize: 20,
                  height: 1.05,
                  fontWeight: FontWeight.w800,
                  color: AppColors.background,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                'Scan 3 stations this week to\nunlock a free Latte at Central St.',
                style: TextStyle(
                  fontSize: 14,
                  height: 1.35,
                  color: AppColors.background.withValues(alpha: 0.92),
                  fontWeight: FontWeight.w500,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _HeroDots extends StatelessWidget {
  const _HeroDots();

  @override
  Widget build(BuildContext context) {
    return const Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        _Dot(isActive: false),
        SizedBox(width: 6),
        _Dot(isActive: true),
        SizedBox(width: 6),
        _Dot(isActive: false),
      ],
    );
  }
}

class _Dot extends StatelessWidget {
  const _Dot({required this.isActive});

  final bool isActive;

  @override
  Widget build(BuildContext context) {
    return AnimatedContainer(
      duration: const Duration(milliseconds: 180),
      width: isActive ? 16 : 6,
      height: 6,
      decoration: BoxDecoration(
        color: isActive ? AppColors.brandBlue : AppColors.border,
        borderRadius: BorderRadius.circular(999),
      ),
    );
  }
}

class _ProgressCard extends StatelessWidget {
  const _ProgressCard();

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.fromLTRB(18, 18, 18, 20),
      decoration: BoxDecoration(
        color: AppColors.background,
        borderRadius: BorderRadius.circular(22),
        border: Border.all(color: const Color(0xFFE8EDF3)),
      ),
      child: Column(
        children: [
          Align(
            alignment: Alignment.topRight,
            child: Icon(
              Icons.emoji_events_outlined,
              color: AppColors.brandRed.withValues(alpha: 0.18),
              size: 30,
            ),
          ),
          const SizedBox(height: 2),
          SizedBox(
            width: 118,
            height: 118,
            child: Stack(
              alignment: Alignment.center,
              children: [
                const SizedBox(
                  width: 118,
                  height: 118,
                  child: CircularProgressIndicator(
                    value: 5 / 14,
                    strokeWidth: 7,
                    backgroundColor: Color(0xFFF0F3F8),
                    valueColor: AlwaysStoppedAnimation<Color>(
                      AppColors.brandBlue,
                    ),
                    strokeCap: StrokeCap.round,
                  ),
                ),
                Container(
                  width: 90,
                  height: 90,
                  decoration: const BoxDecoration(
                    color: Color(0xFFFAFBFD),
                    shape: BoxShape.circle,
                  ),
                ),
                const Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(
                      '5/14',
                      style: TextStyle(
                        fontSize: 28,
                        fontWeight: FontWeight.w800,
                        color: AppColors.brandBlue,
                      ),
                    ),
                    SizedBox(height: 2),
                    Text(
                      'STAMPS',
                      style: TextStyle(
                        fontSize: 11,
                        fontWeight: FontWeight.w700,
                        letterSpacing: 0.6,
                        color: AppColors.textPrimary,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
          const SizedBox(height: 18),
          const Text(
            'Global Explorer',
            style: TextStyle(
              fontSize: 17,
              fontWeight: FontWeight.w800,
              color: AppColors.textPrimary,
            ),
          ),
          const SizedBox(height: 8),
          const Text(
            '9 more stations to unlock the Silver Badge',
            textAlign: TextAlign.center,
            style: TextStyle(
              fontSize: 14,
              color: AppColors.textMuted,
              fontWeight: FontWeight.w500,
            ),
          ),
          const SizedBox(height: 18),
          const Row(
            children: [
              Expanded(
                child: _MilestoneChip(
                  label: '3 Stamps',
                  isActive: true,
                  icon: Icons.check_circle_outline_rounded,
                ),
              ),
              SizedBox(width: 10),
              Expanded(
                child: _MilestoneChip(
                  label: '7 Stamps',
                  icon: Icons.card_giftcard_outlined,
                ),
              ),
              SizedBox(width: 10),
              Expanded(
                child: _MilestoneChip(
                  label: '14 Stamps',
                  icon: Icons.card_giftcard_outlined,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _MilestoneChip extends StatelessWidget {
  const _MilestoneChip({
    required this.label,
    required this.icon,
    this.isActive = false,
  });

  final String label;
  final IconData icon;
  final bool isActive;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(vertical: 12),
      decoration: BoxDecoration(
        color: isActive ? const Color(0xFFF4F9FF) : AppColors.background,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(
          color: isActive ? const Color(0xFFB4D8FF) : AppColors.border,
        ),
      ),
      child: Column(
        children: [
          Container(
            width: 28,
            height: 28,
            decoration: BoxDecoration(
              color: isActive ? AppColors.brandBlue : const Color(0xFFF2F4F7),
              shape: BoxShape.circle,
            ),
            child: Icon(
              icon,
              size: 16,
              color: isActive ? AppColors.background : AppColors.textMuted,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            label,
            style: TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w700,
              color: isActive ? AppColors.brandBlue : AppColors.textPrimary,
            ),
          ),
        ],
      ),
    );
  }
}

class _SectionHeader extends StatelessWidget {
  const _SectionHeader({
    required this.title,
    this.icon = Icons.watch_later_outlined,
    this.trailing,
  });

  final String title;
  final IconData icon;
  final Widget? trailing;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Icon(
          icon,
          size: 18,
          color: AppColors.brandBlue,
        ),
        const SizedBox(width: 8),
        Expanded(
          child: Text(
            title,
            style: const TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w800,
              color: AppColors.textPrimary,
            ),
          ),
        ),
        if (trailing != null) trailing!,
      ],
    );
  }
}

class _PartnerMarquee extends StatefulWidget {
  const _PartnerMarquee({
    required this.brands,
  });

  final List<_PartnerBrand> brands;

  @override
  State<_PartnerMarquee> createState() => _PartnerMarqueeState();
}

class _PartnerMarqueeState extends State<_PartnerMarquee>
    with SingleTickerProviderStateMixin {
  late final AnimationController controller;

  static const double _cardWidth = 172;
  static const double _cardGap = 12;

  @override
  void initState() {
    super.initState();
    controller = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 28),
    )..repeat();
  }

  @override
  void dispose() {
    controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final repeatedBrands = [...widget.brands, ...widget.brands];
    final loopWidth = widget.brands.length * (_cardWidth + _cardGap);
    final marqueeWidth = 28 +
        repeatedBrands.length * _cardWidth +
        (repeatedBrands.length - 1) * _cardGap;

    return Container(
      height: 88,
      padding: const EdgeInsets.symmetric(vertical: 10),
      decoration: BoxDecoration(
        color: AppColors.background,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: const Color(0xFFE7EBF2)),
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(18),
        child: AnimatedBuilder(
          animation: controller,
          builder: (context, child) {
            final offset = -(controller.value * loopWidth);
            return Transform.translate(
              offset: Offset(offset, 0),
              child: child,
            );
          },
          child: OverflowBox(
            alignment: Alignment.centerLeft,
            minWidth: marqueeWidth,
            maxWidth: marqueeWidth,
            child: Row(
              children: [
                const SizedBox(width: 14),
                ...List.generate(repeatedBrands.length, (index) {
                  return Padding(
                    padding: EdgeInsets.only(
                      right: index == repeatedBrands.length - 1 ? 14 : _cardGap,
                    ),
                    child: _PartnerLogoCard(brand: repeatedBrands[index]),
                  );
                }),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class _PartnerLogoCard extends StatelessWidget {
  const _PartnerLogoCard({required this.brand});

  final _PartnerBrand brand;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: 172,
      child: ClipRRect(
        borderRadius: BorderRadius.circular(16),
        child: Container(
          decoration: BoxDecoration(
            color: brand.background,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: brand.foreground.withValues(alpha: 0.14)),
          ),
          child: Stack(
            children: [
              if (brand.backgroundUrl != null &&
                  brand.backgroundUrl!.isNotEmpty)
                Positioned.fill(
                  child: ClipRRect(
                    borderRadius: BorderRadius.circular(16),
                    child: Image.network(
                      brand.backgroundUrl!,
                      fit: BoxFit.cover,
                      alignment: Alignment.centerLeft,
                      errorBuilder: (context, error, stackTrace) {
                        return const SizedBox.shrink();
                      },
                    ),
                  ),
                ),
              Positioned.fill(
                child: DecoratedBox(
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(16),
                    color: brand.background.withValues(
                      alpha: brand.backgroundUrl != null &&
                              brand.backgroundUrl!.isNotEmpty
                          ? 0.28
                          : 1,
                    ),
                  ),
                ),
              ),
              Positioned.fill(
                child: Padding(
                  padding:
                      const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                  child: Row(
                    children: [
                      Container(
                        width: 38,
                        height: 38,
                        decoration: BoxDecoration(
                          color: brand.logoUrl == null
                              ? brand.foreground
                              : AppColors.background,
                          shape: BoxShape.circle,
                          boxShadow: [
                            BoxShadow(
                              color: brand.foreground.withValues(alpha: 0.18),
                              blurRadius: 10,
                              offset: const Offset(0, 4),
                            ),
                          ],
                        ),
                        child: brand.logoUrl != null
                            ? ClipOval(
                                child: Image.network(
                                  brand.logoUrl!,
                                  fit: BoxFit.cover,
                                  width: 38,
                                  height: 38,
                                  errorBuilder: (context, error, stackTrace) {
                                    return _PartnerLogoFallback(brand: brand);
                                  },
                                ),
                              )
                            : _PartnerLogoFallback(brand: brand),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Text(
                          brand.name,
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                          style: TextStyle(
                            fontSize: 14,
                            height: 1.05,
                            fontWeight: FontWeight.w900,
                            fontStyle: brand.name == 'Highlands Coffee'
                                ? FontStyle.italic
                                : FontStyle.normal,
                            letterSpacing:
                                brand.name == 'PHUC LONG' ? 1.2 : 0.15,
                            color: brand.foreground,
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _PartnerLogoFallback extends StatelessWidget {
  const _PartnerLogoFallback({required this.brand});

  final _PartnerBrand brand;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Text(
        brand.shortName,
        style: TextStyle(
          fontSize: brand.shortName.length > 2 ? 10 : 14,
          fontWeight: FontWeight.w900,
          letterSpacing: 0.4,
          color:
              brand.logoUrl == null ? AppColors.background : brand.foreground,
        ),
      ),
    );
  }
}

class _VoucherSection extends StatelessWidget {
  const _VoucherSection({
    required this.categories,
    required this.deals,
  });

  final List<_VoucherCategory> categories;
  final List<_VoucherDeal> deals;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.only(bottom: 12),
      decoration: BoxDecoration(
        color: AppColors.background,
        borderRadius: BorderRadius.circular(28),
        border: Border.all(color: const Color(0xFFE5EAF1)),
        boxShadow: const [
          BoxShadow(
            color: Color(0x1009599E),
            blurRadius: 18,
            offset: Offset(0, 10),
          ),
        ],
      ),
      child: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(18, 18, 18, 14),
            child: Row(
              children: [
                const _MacDot(color: Color(0xFFFF5F57)),
                const SizedBox(width: 10),
                const _MacDot(color: Color(0xFFFEBB2E)),
                const SizedBox(width: 10),
                const _MacDot(color: Color(0xFF28C840)),
                const SizedBox(width: 14),
                Expanded(
                  child: Container(
                    height: 54,
                    padding: const EdgeInsets.symmetric(horizontal: 18),
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(999),
                      border: Border.all(color: const Color(0xFFD8DDE5)),
                    ),
                    child: const Row(
                      children: [
                        SizedBox(width: 10),
                        Expanded(
                          child: Text(
                            ' Vouchers',
                            style: TextStyle(
                              fontSize: 15,
                              color: AppColors.textMuted,
                              fontWeight: FontWeight.w500,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ),
          const Divider(height: 1, color: Color(0xFFE5EAF1)),
          SizedBox(
            height: 72,
            child: ListView.separated(
              padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 14),
              scrollDirection: Axis.horizontal,
              itemBuilder: (context, index) {
                return _VoucherCategoryChip(category: categories[index]);
              },
              separatorBuilder: (context, index) => const SizedBox(width: 10),
              itemCount: categories.length,
            ),
          ),
          const Divider(height: 1, color: Color(0xFFE5EAF1)),
          Padding(
            padding: const EdgeInsets.fromLTRB(18, 16, 18, 0),
            child: Column(
              children: [
                for (int index = 0; index < deals.length; index++) ...[
                  _VoucherDealCard(deal: deals[index]),
                  if (index != deals.length - 1) const SizedBox(height: 16),
                ],
              ],
            ),
          ),
          const SizedBox(height: 14),
          const Padding(
            padding: EdgeInsets.symmetric(horizontal: 18),
            child: Center(
              child: Text(
                '4 vouchers available',
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 15,
                  color: AppColors.textMuted,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _MacDot extends StatelessWidget {
  const _MacDot({required this.color});

  final Color color;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 14,
      height: 14,
      decoration: BoxDecoration(
        color: color,
        shape: BoxShape.circle,
      ),
    );
  }
}

class _VoucherCategoryChip extends StatelessWidget {
  const _VoucherCategoryChip({required this.category});

  final _VoucherCategory category;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 22, vertical: 12),
      decoration: BoxDecoration(
        color: category.isActive ? AppColors.textPrimary : AppColors.background,
        borderRadius: BorderRadius.circular(999),
        border: Border.all(
          color: category.isActive
              ? AppColors.textPrimary
              : const Color(0xFFD8DDE5),
        ),
      ),
      child: Text(
        category.label,
        style: TextStyle(
          fontSize: 15,
          fontWeight: FontWeight.w700,
          color:
              category.isActive ? AppColors.background : AppColors.textPrimary,
        ),
      ),
    );
  }
}

class _VoucherDealCard extends StatelessWidget {
  const _VoucherDealCard({required this.deal});

  final _VoucherDeal deal;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: AppColors.background,
        borderRadius: BorderRadius.circular(26),
        border: Border.all(color: const Color(0xFFD8DDE5)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          ClipRRect(
            borderRadius: const BorderRadius.vertical(top: Radius.circular(26)),
            child: Container(
              height: 122,
              width: double.infinity,
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  colors: deal.colors,
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
              ),
              child: Stack(
                children: [
                  Positioned.fill(
                    child: _VoucherHeroImage(deal: deal),
                  ),
                  Positioned.fill(
                    child: DecoratedBox(
                      decoration: BoxDecoration(
                        gradient: LinearGradient(
                          begin: Alignment.bottomCenter,
                          end: Alignment.topCenter,
                          colors: [
                            Colors.black.withValues(alpha: 0.18),
                            Colors.transparent,
                          ],
                        ),
                      ),
                    ),
                  ),
                  Positioned(
                    left: 10,
                    bottom: 10,
                    child: Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 12,
                        vertical: 8,
                      ),
                      decoration: BoxDecoration(
                        color: const Color(0xCC202917),
                        borderRadius: BorderRadius.circular(999),
                      ),
                      child: Row(
                        children: [
                          const Icon(
                            Icons.star_rounded,
                            size: 15,
                            color: AppColors.brandRed,
                          ),
                          const SizedBox(width: 6),
                          Text(
                            deal.rating == 5 ? '5' : deal.rating.toString(),
                            style: const TextStyle(
                              fontSize: 14,
                              fontWeight: FontWeight.w800,
                              color: AppColors.background,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                  Positioned(
                    right: 12,
                    top: 10,
                    child: Row(
                      children: [
                        const SizedBox(width: 8),
                        CircleAvatar(
                          radius: 21,
                          backgroundColor: AppColors.background,
                          child: Icon(
                            deal.isFavorite
                                ? Icons.favorite_rounded
                                : Icons.favorite_border_rounded,
                            color: deal.isFavorite
                                ? AppColors.brandRed
                                : AppColors.textMuted,
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.fromLTRB(18, 16, 18, 18),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  deal.title,
                  style: const TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.w800,
                    color: AppColors.brandBlue,
                  ),
                ),
                const SizedBox(height: 8),
                Row(
                  children: [
                    const Icon(
                      Icons.location_on_outlined,
                      size: 18,
                      color: AppColors.textMuted,
                    ),
                    const SizedBox(width: 4),
                    Text(
                      deal.subtitle,
                      style: const TextStyle(
                        fontSize: 14,
                        color: AppColors.textMuted,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Expanded(
                      child: Wrap(
                        spacing: 14,
                        runSpacing: 8,
                        children: [
                          _VoucherMetaItem(
                            icon: Icons.local_offer_outlined,
                            label: deal.meta[0],
                          ),
                          _VoucherMetaItem(
                            icon: Icons.card_giftcard_outlined,
                            label: deal.meta[1],
                          ),
                          _VoucherMetaItem(
                            icon: Icons.wifi_rounded,
                            label: deal.meta[2],
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(width: 12),
                    Padding(
                      padding: const EdgeInsets.only(top: 1),
                      child: RichText(
                        text: TextSpan(
                          children: [
                            TextSpan(
                              text: deal.priceLabel,
                              style: const TextStyle(
                                fontSize: 18,
                                fontWeight: FontWeight.w900,
                                color: AppColors.textPrimary,
                              ),
                            ),
                            TextSpan(
                              text: '/${deal.unitLabel}',
                              style: const TextStyle(
                                fontSize: 13,
                                fontWeight: FontWeight.w500,
                                color: AppColors.textMuted,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _VoucherHeroImage extends StatelessWidget {
  const _VoucherHeroImage({required this.deal});

  final _VoucherDeal deal;

  @override
  Widget build(BuildContext context) {
    if (deal.imageUrl == null) {
      return const SizedBox.shrink();
    }

    return Image.network(
      deal.imageUrl!,
      fit: BoxFit.cover,
      errorBuilder: (context, error, stackTrace) {
        return const SizedBox.shrink();
      },
    );
  }
}

class _VoucherMetaItem extends StatelessWidget {
  const _VoucherMetaItem({required this.icon, required this.label});

  final IconData icon;
  final String label;

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Icon(
          icon,
          size: 18,
          color: AppColors.textMuted,
        ),
        const SizedBox(width: 6),
        Text(
          label,
          style: const TextStyle(
            fontSize: 14,
            color: AppColors.textMuted,
            fontWeight: FontWeight.w500,
          ),
        ),
      ],
    );
  }
}

class _RecentStampCard extends StatelessWidget {
  const _RecentStampCard({required this.stamp});

  final _RecentStamp stamp;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: 92,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: 92,
            height: 92,
            padding: const EdgeInsets.all(6),
            decoration: BoxDecoration(
              gradient: LinearGradient(
                colors: stamp.colors,
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
              borderRadius: BorderRadius.circular(18),
              border: Border.all(color: const Color(0xFFE4ECF4)),
            ),
            child: Stack(
              alignment: Alignment.center,
              children: [
                Container(
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(14),
                    border: Border.all(
                      color: AppColors.background.withValues(alpha: 0.65),
                      style: BorderStyle.solid,
                    ),
                  ),
                ),
                CircleAvatar(
                  radius: 15,
                  backgroundColor: AppColors.background.withValues(alpha: 0.9),
                  child: Icon(
                    Icons.check_circle_outline_rounded,
                    color: stamp.colors.last,
                    size: 18,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 8),
          Text(
            stamp.title,
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: const TextStyle(
              fontSize: 13,
              fontWeight: FontWeight.w800,
              color: AppColors.textPrimary,
            ),
          ),
          const SizedBox(height: 2),
          Text(
            stamp.time,
            style: const TextStyle(
              fontSize: 11,
              color: AppColors.textMuted,
              fontWeight: FontWeight.w500,
            ),
          ),
        ],
      ),
    );
  }
}

class _ScanNowCard extends StatelessWidget {
  const _ScanNowCard();

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 20),
      decoration: BoxDecoration(
        color: AppColors.brandRed,
        borderRadius: BorderRadius.circular(18),
        boxShadow: [
          BoxShadow(
            color: AppColors.brandRed.withValues(alpha: 0.2),
            blurRadius: 20,
            offset: const Offset(0, 12),
          ),
        ],
      ),
      child: Row(
        children: [
          const Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'STAMP NOW!',
                  style: TextStyle(
                    fontSize: 18,
                    fontStyle: FontStyle.italic,
                    fontWeight: FontWeight.w900,
                    color: AppColors.background,
                  ),
                ),
                SizedBox(height: 6),
                Text(
                  'Found a QR code at the station?',
                  style: TextStyle(
                    fontSize: 13,
                    fontWeight: FontWeight.w600,
                    color: AppColors.background,
                  ),
                ),
              ],
            ),
          ),
          Container(
            width: 50,
            height: 50,
            decoration: BoxDecoration(
              color: AppColors.background.withValues(alpha: 0.16),
              shape: BoxShape.circle,
            ),
            child: const Icon(
              Icons.qr_code_scanner_rounded,
              color: AppColors.background,
              size: 28,
            ),
          ),
        ],
      ),
    );
  }
}

class _QuickActionCard extends StatelessWidget {
  const _QuickActionCard({required this.action});

  final _QuickAction action;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: action.routeName == null
            ? null
            : () => Navigator.of(context).pushNamed(action.routeName!),
        borderRadius: BorderRadius.circular(16),
        child: Ink(
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
          decoration: BoxDecoration(
            color: AppColors.background,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: const Color(0xFFE7EBF2)),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(action.icon, color: action.accent, size: 21),
              const SizedBox(height: 12),
              Text(
                action.label,
                style: const TextStyle(
                  fontSize: 15,
                  fontWeight: FontWeight.w800,
                  color: AppColors.textPrimary,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _CommunityCard extends StatelessWidget {
  const _CommunityCard();

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      decoration: BoxDecoration(
        color: AppColors.background,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: const Color(0xFFE8EDF3)),
      ),
      child: Row(
        children: [
          const SizedBox(
            width: 52,
            height: 26,
            child: Stack(
              children: [
                _AvatarBubble(left: 0, color: Color(0xFF8BB8FF)),
                _AvatarBubble(left: 14, color: Color(0xFFB8A1FF)),
                _AvatarBubble(left: 28, color: Color(0xFFE88C7B)),
              ],
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: RichText(
              text: const TextSpan(
                style: TextStyle(
                  fontSize: 13,
                  height: 1.35,
                  color: AppColors.textMuted,
                  fontWeight: FontWeight.w500,
                ),
                children: [
                  TextSpan(
                    text: '1,240 commuters',
                    style: TextStyle(
                      color: AppColors.textPrimary,
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                  TextSpan(text: ' scanned a station in the last hour!'),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _AvatarBubble extends StatelessWidget {
  const _AvatarBubble({
    required this.left,
    required this.color,
  });

  final double left;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return Positioned(
      left: left,
      child: CircleAvatar(
        radius: 13,
        backgroundColor: AppColors.background,
        child: CircleAvatar(
          radius: 11,
          backgroundColor: color,
          child: const Icon(
            Icons.person,
            size: 12,
            color: AppColors.background,
          ),
        ),
      ),
    );
  }
}

class _HomeBottomBar extends StatelessWidget {
  const _HomeBottomBar({
    required this.selectedIndex,
    required this.onSelected,
  });

  final int selectedIndex;
  final ValueChanged<int> onSelected;

  @override
  Widget build(BuildContext context) {
    return BottomAppBar(
      height: 78,
      padding: const EdgeInsets.symmetric(horizontal: 8),
      color: AppColors.background,
      surfaceTintColor: AppColors.background,
      child: Row(
        children: [
          Expanded(
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                _NavItem(
                  icon: Icons.home_outlined,
                  label: 'Home',
                  isActive: selectedIndex == 0,
                  onTap: () => onSelected(0),
                ),
                _NavItem(
                  icon: Icons.book_outlined,
                  label: 'Book',
                  isActive: selectedIndex == 1,
                  onTap: () => onSelected(1),
                ),
              ],
            ),
          ),
          const SizedBox(width: 56),
          Expanded(
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                _NavItem(
                  icon: Icons.format_list_bulleted_rounded,
                  label: 'Stations',
                  isActive: selectedIndex == 2,
                  onTap: () => onSelected(2),
                ),
                _NavItem(
                  icon: Icons.person_outline_rounded,
                  label: 'Profile',
                  isActive: selectedIndex == 3,
                  onTap: () => onSelected(3),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _NavItem extends StatelessWidget {
  const _NavItem({
    required this.icon,
    required this.label,
    required this.isActive,
    required this.onTap,
  });

  final IconData icon;
  final String label;
  final bool isActive;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(14),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 6),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              icon,
              color: isActive ? AppColors.brandBlue : AppColors.textPrimary,
            ),
            const SizedBox(height: 4),
            Text(
              label,
              style: TextStyle(
                fontSize: 11,
                fontWeight: FontWeight.w700,
                color: isActive ? AppColors.brandBlue : AppColors.textPrimary,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _RecentStamp {
  const _RecentStamp({
    required this.title,
    required this.time,
    required this.colors,
  });

  final String title;
  final String time;
  final List<Color> colors;
}

class _QuickAction {
  const _QuickAction({
    required this.icon,
    required this.label,
    required this.accent,
    this.routeName,
  });

  final IconData icon;
  final String label;
  final Color accent;
  final String? routeName;
}

class _PartnerBrand {
  const _PartnerBrand({
    required this.name,
    required this.shortName,
    required this.foreground,
    required this.background,
    this.backgroundUrl,
    this.logoUrl,
  });

  final String name;
  final String shortName;
  final Color foreground;
  final Color background;
  final String? backgroundUrl;
  final String? logoUrl;
}

class _VoucherCategory {
  const _VoucherCategory({
    required this.label,
    this.isActive = false,
  });

  final String label;
  final bool isActive;
}

class _VoucherDeal {
  const _VoucherDeal({
    required this.title,
    required this.subtitle,
    required this.rating,
    required this.priceLabel,
    required this.unitLabel,
    required this.meta,
    required this.colors,
    required this.brandLabel,
    this.imageUrl,
    this.isFavorite = false,
  });

  final String title;
  final String subtitle;
  final double rating;
  final String priceLabel;
  final String unitLabel;
  final List<String> meta;
  final List<Color> colors;
  final String brandLabel;
  final String? imageUrl;
  final bool isFavorite;
}
