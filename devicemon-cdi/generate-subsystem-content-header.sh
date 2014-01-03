file=$1
file1="$file.1"
file2="$file.2"

shift

echo "Subsystem-Content: " > $file1

for var in "$@"
do
   # echo $var
   fname="${var##*/}"
   bsn="${fname%-*}"

   if [[ "$bsn" != *slf4j* ]] && [[ "$bsn" != *geronimo* ]]
   then
       suffix="${fname##*-}"
       version="${suffix%%.jar}"
       echo "$bsn;version=$version" 
       echo "$bsn;version=$version," >> $file1
   else
       echo "Excluding: $bsn"
   fi
done

tr -d '\n' < $file1 > $file2

# remove the trailing comma:
sed '$s/,$//' < "$file2" > "$file"
echo "" >> "$file"

rm $file1 $file2

