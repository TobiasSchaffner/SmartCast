sudo apt-get update
sudo apt-get install xinetd omxplayer pip
sudo pip install --upgrade youtube-dl 
sudo chmod a+rw /dev/vchiq
sudo adduser smartcast
su smartcast
mkdir ~/smartcast
cp .* ~/smartcast/
cd ~smartcast
chmod +x smartcast.sh
chmod +x youtube-music/youtube-music.sh
sudo cp smartcast /etc/xinetd.d/
sudo service xinetd restart
