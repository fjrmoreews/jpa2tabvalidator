rm -rf ./lib
mvn -Dhttps.protocols=TLSv1.2  clean dependency:copy-dependencies

mkdir -p target/dependency
mkdir -p ./lib
mv target/dependency/* ./lib/
rm -rf target

