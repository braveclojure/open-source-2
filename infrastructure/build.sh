cd ..
boot build
chmod 755 target/build/app.jar
cp target/build/app.jar infrastructure/ansible/files/app.jar
