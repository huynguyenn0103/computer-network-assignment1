# bittorrent

This project is done as part of CodeCrafters [challenge](https://app.codecrafters.io/courses/bittorrent/overview).

To test the project, use following commands line arguments:

1. `decode 10:1234567890` to decode a Bencoded string
2. `decode l5:helloi52e10:12345678902:22i-23ee` to decode a Bencoded list
3. `decode d1:kd3:abclli-3eeli4e4:defgeee2:k21:ve` to decode a Bencoded dictionary
4. `download_piece -o <torrentoutput.file> sample.torrent <piece_num>` to download a particular piece number from the torrent file and save the output to torrentoutput.file.
5. `download -o <torrentoutput.file> sample.torrent` to download the entire file from the torrent and save the output to torrentoutput.file.
