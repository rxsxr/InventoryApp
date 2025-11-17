#!/bin/sh

TMPF=$(mktemp jqout.XXXXX)

jq -r '.["trans"].[] | [ .["idName"], .["dateStamp"] ] | @tsv '<  output.json  > $TMPF

PREU=$(sort < $TMPF | wc -l)
PSTU=$(sort -u $TMPF | wc -l)

printf "NUniq\tUniq\n"
printf "%d\t%d\n" $PREU $PSTU

rm $TMPF
