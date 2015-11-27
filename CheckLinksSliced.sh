#!/bin/bash

if [ "$#" -lt 4 ]; then
  echo "usage: <inFile> <outGoodFile> <outBrokenFile> <outMongoBrokenFile> [<encoding>]"
  exit 1
fi

file="$1"
qtt=50000
lines=`wc -l $file | cut -d ' ' -f 1`
div=`expr $lines / $qtt`
mod=`expr $lines % $qtt`

if [ -f $2 ]; then
  rm $2
fi
if [ -f $3 ]; then
  rm $3
fi
if [ -f $4 ]; then
  rm $4
fi

if [ `expr $mod % $qtt` -eq 0 ]
then
  times=$div
else
  times=`expr $div + 1`
fi

rem=$lines

for i in `seq 1 $times`
do
  tail -$rem $file | head -$qtt > xx
  echo
  echo "-----------------------------------------------------------------------"
  echo "CheckLinksApplication - checking $qtt links from link number `expr \( $i - 1 \) \* $qtt \+ 1`"
  sbt "run-main org.bireme.cl.CheckLinksApplication xx $2 $3 $5 --append"
  rem=`expr $rem - $qtt`
done

rm xx

echo "Checking if broken urls were broken some time ago ..."
sbt "run-main org.bireme.murl.MongoUrlApp mongodb.bireme.br $3 $4 $5"
