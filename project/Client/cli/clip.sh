#!/bin/bash
LIBPATH=.
source $LIBPATH/config.sh

usage () {
	echo "${_BASENAME}: usage"
	echo ""
	echo "PARAMETERS:"
	echo "  -g=PATH             get path."
	echo "  -s=PATH             set path."
	echo "  -k=PATH             get keys."
	echo "  -u=PATH             (CAS MD5) submit value to map."
	echo "  -U=PATH             (UUID)    submit value to map."
	echo "  -d=PATH             del path."
	echo "  -h                  This message."
	exit 0
}

[[ -z "${1}" ]] && usage

# change to english locale!
export LANG="en_US"

while [ $# -gt 0 ]; do
	case ${1} in
		-k=*|--k=*) keypath="$(echo ${1} | awk -F= '{print $2;}')" ;;
		-g=*|--g=*) getpath="$(echo ${1} | awk -F= '{print $2;}')" ;;
		-u=*|--u=*) subpath="$(echo ${1} | awk -F= '{print $2;}')" ;;
		-U=*|--U=*) usubpath="$(echo ${1} | awk -F= '{print $2;}')" ;;
		-s=*|--s=*) setpath="$(echo ${1} | awk -F= '{print $2;}')" ;;
		-d=*|--d=*) delpath="$(echo ${1} | awk -F= '{print $2;}')" ;;
		-v|--v) verbose_mode="true" ;;
		-h|--h|?) usage ;;
		*) usage ;;
		esac
	shift
done
# Source the output file ----------------------------------------------------------
if [ "$setpath" != "" ]; then
    read INPUT_DATA
    curl -s -XPUT -d$($JQPath -c -n --arg output "$INPUT_DATA" '{content: $output}') $PROTO://$KVDN_HOST:$KVDN_PORT/X/$setpath
    if [ "$verbose_mode" = "true" ]; then
     echo >&2 curl -s -XPUT -d$($JQPath -c -n --arg output "$INPUT_DATA" '{content: $output}') $PROTO://$KVDN_HOST:$KVDN_PORT/X/$setpath;
    fi
fi
if [ "$subpath" != "" ]; then
    read INPUT_DATA
    curl -s -XPOST -d$($JQPath -c -n --arg output "$INPUT_DATA" '{content: $output}') $PROTO://$KVDN_HOST:$KVDN_PORT/X/$subpath
    if [ "$verbose_mode" = "true" ]; then
     echo >&2 curl -s -XPOST -d$($JQPath -c -n --arg output "$INPUT_DATA" '{content: $output}') $PROTO://$KVDN_HOST:$KVDN_PORT/X/$subpath;
    fi
fi
if [ "$usubpath" != "" ]; then
    read INPUT_DATA
    curl -s -XPOST -d$($JQPath -c -n --arg output "$INPUT_DATA" '{content: $output}') $PROTO://$KVDN_HOST:$KVDN_PORT/U/$usubpath
    if [ "$verbose_mode" = "true" ]; then
     echo >&2 curl -s -XPOST -d$($JQPath -c -n --arg output "$INPUT_DATA" '{content: $output}') $PROTO://$KVDN_HOST:$KVDN_PORT/U/$usubpath;
    fi
fi
if [ "$getpath" != "" ]; then
    curl -s -XGET  $PROTO://$KVDN_HOST:$KVDN_PORT/X/$getpath
    if [ "$verbose_mode" = "true" ]; then echo curl -s -XGET  $PROTO://$KVDN_HOST:$KVDN_PORT/X/$getpath ; fi

fi
if [ "$keypath" != "" ]; then
    curl -s -XGET  $PROTO://$KVDN_HOST:$KVDN_PORT/KEYS/$keypath
    if [ "$verbose_mode" = "true" ]; then echo  curl -s -XGET  $PROTO://$KVDN_HOST:$KVDN_PORT/KEYS/$keypath ; fi

fi

if [ "$delpath" != "" ]; then
    curl -s -XDELETE  $PROTO://$KVDN_HOST:$KVDN_PORT/X/$delpath
    if [ "$verbose_mode" = "true" ]; then echo curl -s -XDELETE  $PROTO://$KVDN_HOST:$KVDN_PORT/X/$delpath ; fi

fi


# Display arguments
if [ "$verbose_mode" = "true" ]; then
	echo >&2 "Verbose mode ON"
	echo >&2 "setpath  : $setpath"
	echo >&2 "delpath: $delpath"
	echo >&2 "clpth : $clpth"
	echo >&2 "basepath : $PROTO://$KVDN_HOST:$KVDN_PORT/X/"
fi

# Check if input file exists
if [ "$verbose_mode" = "true" ]; then echo >&2 "Done."; fi

exit 0
