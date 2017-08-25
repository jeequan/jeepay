FROM java:8
VOLUME /tmp
RUN mkdir /app
ADD xxpay-config.jar /app/app.jar
ADD runboot.sh /app/
RUN bash -c 'touch /app/app.jar'
WORKDIR /app
RUN chmod a+x runboot.sh
EXPOSE 2020
CMD /app/runboot.sh