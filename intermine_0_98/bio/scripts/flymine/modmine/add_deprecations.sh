#!/bin/bash
#
# default usage: add_deprecation.sh
#
# TODO: build file, clean
#
# sc

#ARKDIR=/micklem/releases/modmine
DATADIR=/micklem/data/modmine/subs/chado
LOGDIR=$DATADIR/logs
FTPARK=$DATADIR/ark

INTERACT=y
DOIT=y
INFILE=$DATADIR/deprecations

DBHOST=modfast
DBUSER=modmine

#PRO="celnikerlai"
PRO="lieb henikoff macalpine oliver snyder karpen white celnikerlai waterstonpiano"

progname=$0

umask 0002

function usage () {
	cat <<EOF

Usage:
$progname [-b] 
	-b: batch mode (all operations are executed without warning)
    -m: using the specified host (default: modfast)

examples:

$progname
$progname -b idem, batch mode (careful!)
$progname -m modprod1 using dbhost modprod1

EOF
	exit 0
}

echo

while getopts ":b" opt; do
	case $opt in
	b )  echo "- BATCH mode" ; INTERACT=n;;
	m )  DBHOST=$OPTARG; echo "- Using db host $DBHOST";;
	h )  usage ;;
	\?)  usage ;;
	esac
done

shift $(($OPTIND - 1))



LOG="$LOGDIR/deprecations."`date "+%y%m%d.%H%M"`


function interact {
if [ "$INTERACT" = "y" ]
then
echo "** $1"
echo "Press return to continue or [n] to skip this step (^C to exit).."
echo -n "->"
read DOIT
fi

}


function build_file {
# TODO: need to unify the first field (see 2675,2878 plus already dead 2801 and 2812)
# get the list of deprecated entries with their replacement

grep released $DATADIR/ftplist | grep true | awk '$2 == "true" {print $3","$1 }' | grep -v unknown > $DATADIR/depr
grep released $DATADIR/ftplist | grep true | awk '$3 == "true" {print $4","$1 }' >> $DATADIR/depr
grep released $DATADIR/ftplist | grep true | awk '$4 == "true" {print $5","$1 }' >> $DATADIR/depr

mv $DATADIR/deprecations $FTPARK/dep.`date "+%y%m%d"`
sort -u $DATADIR/depr > $DATADIR/deprecations

#awk '{print $1}' $DATADIR/deprecation.table > $DATADIR/all.dead
#awk '{print $1}' $DATADIR/superseded.table >> $DATADIR/all.dead

}



function deprecates {
RETURNDIR=$PWD
cd $DATADIR
LOOPVAR=`cat $INFILE`

for p in $PRO
do
CHADODB="modchado-$p"

echo "=========== $p.."
echo "$p project" >> $LOG
echo >> $LOG

TYPEID=`psql -h $DBHOST -d $CHADODB -U $DBUSER -q -t -c "select cvterm_id from cvterm where name = 'string';"`

for line in $LOOPVAR
do

DCCID=`echo $line | cut -f 1 -d,`
REPLACED=`echo $line | cut --complement -f 1 -d,`

#echo $line
#echo "$DCCID replaces $REPLACED"

ISIN=`psql -h $DBHOST -d $CHADODB -U $DBUSER -q -t -c "select experiment_id from experiment_prop where name = 'dcc_id' and  value='$DCCID';"`
if [ -n "$ISIN" ]
then
# check if already deleted (add exp_id?)
ISDEL=`psql -h $DBHOST -d $CHADODB -U $DBUSER -q -t -c "select experiment_id from experiment_prop where name = 'deleted' and  value='$DCCID';"`
  if [ -n "$ISDEL" ]
  then
    echo "Submission $DCCID is already deleted in $CHADODB.."
  else
    HASDEPR=`psql -h $DBHOST -d $CHADODB -U $DBUSER -q -t -A -c "select value from experiment_prop where name = 'replaces' and  experiment_id='$ISIN';"`
    if [ -z "$HASDEPR" ]
    then
      # if not, add deprecations
      echo "Submission $DCCID: adding replaces $REPLACED in $CHADODB.."
      echo "$DCCID: adding replaces $REPLACED" >> $LOG
      psql -h $DBHOST -d $CHADODB -U $DBUSER -c "insert into experiment_prop (experiment_id, name, value, type_id) values ('$ISIN', 'replaces', '$REPLACED', $TYPEID);"
    #elif [ $HASDEPR != $REPLACED -a sustring ]
    elif [ "$HASDEPR" != "$REPLACED" ]
    then
    #echo --$HASDEPR-- ++$REPLACED++
      echo "Submission $DCCID: updating deprecation list.."
      echo "$DCCID: updating current $HASDEPR with $REPLACED" >> $LOG
      psql -h $DBHOST -d $CHADODB -U $DBUSER -q -t -c "update experiment_prop set value = '$REPLACED' where name = 'replaces' and  experiment_id='$ISIN';"

    #  elif [ $HASDEPR != $REPLACED -a !sustring ]
    #  then
    #  # append to the existing one
    else
    #echo --$HASDEPR-- ++$REPLACED++
      echo "Submission $DCCID is already up to date in $CHADODB.."
      echo "$DCCID: already up to date ($REPLACED)" >> $LOG
    fi 
  fi
fi

done

echo "=========================================" >>$LOG
done

cd $RETURNDIR
echo
}


########################################
#
# MAIN
#
########################################

echo "Using file: "
echo "`ls -oh $INFILE`"
echo "to add/update replace field in submission on $DBHOST for projects: $PRO."
echo 
interact "Adding deprecated/superseded subs "
if [ "$DOIT" != "n" ]
then
#build_file
echo `date "+%y%m%d.%H%M"` >> $LOG

deprecates
fi

echo `date "+%y%m%d.%H%M"` >> $LOG

more $LOG

echo
echo "Log file available: $LOG"
echo "bye!"

