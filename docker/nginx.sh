#!/bin/sh
/etc/nginx
tar -czvf nginx_$(date +'%F_%H-%M-%S').tar.gz nginx.conf sites-available/ sites-enabled/ nginxconfig.io/
tar -xzvf nginxconfig.io-pay.test.com.tar.gz | xargs chmod 0644

# SSL
openssl dhparam -out /etc/nginx/dhparam.pem 2048
mkdir -p /var/www/_letsencrypt
chown www-data /var/www/_letsencrypt

# Certbot 复制页面上所有命令替换下方语句
sed -i -r 's/(listen .*443)/\1; #/g; s/(ssl_(certificate|certificate_key|trusted_certificate) )/#;#\1/g; s/(server \{)/\1\n    ssl off;/g' /etc/nginx/sites-available/pay.test.com.conf
sudo nginx -t && sudo systemctl reload nginx
certbot certonly --webroot -d pay.test.com --email info@pay.test.com -w /var/www/_letsencrypt -n --agree-tos --force-renewal
sed -i -r -z 's/#?; ?#//g; s/(server \{)\n    ssl off;/\1/g' /etc/nginx/sites-available/pay.test.com.conf
sudo nginx -t && sudo systemctl reload nginx

# 结束

echo -e '#!/bin/bash\nnginx -t && systemctl reload nginx' | sudo tee /etc/letsencrypt/renewal-hooks/post/nginx-reload.sh
sudo chmod a+x /etc/letsencrypt/renewal-hooks/post/nginx-reload.sh
sudo nginx -t && sudo systemctl reload nginx
