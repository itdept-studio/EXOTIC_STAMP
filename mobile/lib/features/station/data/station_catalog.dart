class StationItem {
  const StationItem({
    required this.name,
    required this.line,
    required this.distanceKm,
    required this.latitude,
    required this.longitude,
    required this.imagePath,
    required this.isCollected,
  });

  final String name;
  final String line;
  final double distanceKm;
  final double latitude;
  final double longitude;
  final String imagePath;
  final bool isCollected;

  String get heroImagePath {
    if (name.toLowerCase().contains('ba son')) {
      return 'https://lh3.googleusercontent.com/gps-cs-s/AHVAwepKKL08T5WPb2AHbUTKP3cgbiNBlcx-AfedzQukDtzUXp_WAiLfxdZ46IfX7aDMuVtlzvX4y42lyCyy2fKrgsuRT87ihnP4yrYjJXwXqCjesTv5KcnM9QxHLigJ9MotByayYOQ=w1200-h900-k-no';
    }
    return imagePath;
  }
}

const List<StationItem> stationCatalog = [
  StationItem(
    name: 'Ga Bến Thành',
    line: 'Line 1',
    distanceKm: 0.8,
    latitude: 10.77034593193835,
    longitude: 106.69191978139342,
    imagePath:
        'assets/stamps/z7633669581431_66e47f73a613de962410fc4e3ce82538.jpg',
    isCollected: true,
  ),
  StationItem(
    name: 'Ga Nhà Hát Thành Phố',
    line: 'Line 1',
    distanceKm: 1.2,
    latitude: 10.775347509213042,
    longitude: 106.69947922531944,
    imagePath:
        'assets/stamps/z7633669581471_d24496e11ae33e6b864c38edb12d262d.jpg',
    isCollected: true,
  ),
  StationItem(
    name: 'Ga Ba Son',
    line: 'Line 1',
    distanceKm: 2.4,
    latitude: 10.775373677853546,
    longitude: 106.69175440280686,
    imagePath:
        'assets/stamps/z7633669606889_3dd34dbfa8618b2c9b12038b95575238.jpg',
    isCollected: true,
  ),
  StationItem(
    name: 'Ga Bình Thái',
    line: 'Line 1',
    distanceKm: 8.9,
    latitude: 10.832118108163368,
    longitude: 106.7616248253199,
    imagePath:
        'assets/stamps/z7633669757228_5888c62a474a08d929931db5018dbf36.jpg',
    isCollected: false,
  ),
  StationItem(
    name: 'Ga Thủ Đức',
    line: 'Line 1',
    distanceKm: 10.1,
    latitude: 10.845806230360022,
    longitude: 106.76719208139961,
    imagePath:
        'assets/stamps/z7633669834885_7c89fa05dc6bf219e40460efaf9ff5e4.jpg',
    isCollected: false,
  ),
  StationItem(
    name: 'Ga Khu Công Nghệ Cao',
    line: 'Line 1',
    distanceKm: 11.5,
    latitude: 10.858917507666213,
    longitude: 106.78588762532016,
    imagePath:
        'assets/stamps/z7633669972596_414f583f462e8904a2b7021545cc05ba.jpg',
    isCollected: false,
  ),
  StationItem(
    name: 'Ga Suối Tiên',
    line: 'Line 1',
    distanceKm: 13.2,
    latitude: 10.862374399206809,
    longitude: 106.77492837616066,
    imagePath:
        'assets/stamps/z7633670018993_c4527a120b3265f646643f046dd46355.jpg',
    isCollected: true,
  ),
  StationItem(
    name: 'Ga Bến Xe Miền Đông',
    line: 'Line 1',
    distanceKm: 15.0,
    latitude: 10.8799,
    longitude: 106.8166,
    imagePath:
        'assets/stamps/z7633670089957_a017d808dbeda953fe47ea38629b5c4b.jpg',
    isCollected: false,
  ),
  StationItem(
    name: 'Ga Tân Cảng',
    line: 'Line 2',
    distanceKm: 4.1,
    latitude: 10.79865615878271,
    longitude: 106.72064722531941,
    imagePath:
        'assets/stamps/z7633670219952_d91823be36bbb04ef40f2b54ed00a920.jpg',
    isCollected: false,
  ),
  StationItem(
    name: 'Ga An Phú',
    line: 'Line 2',
    distanceKm: 5.4,
    latitude: 10.798682325008931,
    longitude: 106.71292240281011,
    imagePath:
        'assets/stamps/z7633670375782_05f0fec51330ed57474548949cc8c2c1.jpg',
    isCollected: false,
  ),
  StationItem(
    name: 'Ga Phú Hữu',
    line: 'Line 5',
    distanceKm: 6.2,
    latitude: 10.7905,
    longitude: 106.8060,
    imagePath:
        'assets/stamps/z7633670486944_5ef50a8bed4afc1200c13b08e61daca6.jpg',
    isCollected: false,
  ),
  StationItem(
    name: 'Ga Long Trường',
    line: 'Line 5',
    distanceKm: 9.7,
    latitude: 10.8120,
    longitude: 106.8295,
    imagePath:
        'assets/stamps/z7633670582197_7cd23d1bacb7e4e1573692071103bb13.jpg',
    isCollected: false,
  ),
  StationItem(
    name: 'Ga Depot',
    line: 'Line 5',
    distanceKm: 12.6,
    latitude: 10.8510,
    longitude: 106.8150,
    imagePath:
        'assets/stamps/z7633670613254_8e32bf81aa5b4a3da9142f6dd7a2126f.jpg',
    isCollected: false,
  ),
];
