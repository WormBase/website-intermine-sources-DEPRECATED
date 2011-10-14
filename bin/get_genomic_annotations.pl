#!/bin/bash

# Quick data staging script for building a new intermine release.

WSVERSION=$1
DATAROOT=/usr/local/wormbase/intermine/data/${WSVERSION}/

mkdir -p ${DATAROOT}/wormbase/c_elegans/gff3
mkdir -p ${DATAROOT}/wormbase/c_elegans/fasta

# C. elegans genomic annotations
cd ${DATAROOT}/wormbase/c_elegans/gff3
# curl 
# curl -O ftp://www.wormbase.org/pub/wormbase/releases/${WSVERSION}/species/c_elegans/c_elegans.${WSVERSION}.annotations.gff3.gz
# Or via CP
cp /usr/local/ftp/pub/wormbase/releases/${WSVERSION}/species/c_elegans/c_elegans.${WSVERSION}.annotations.gff3.gz c_elegans.current.annotations.gff3.gz

# C. elegans fasta sequence
cp /usr/local/ftp/pub/wormbase/releases/${WSVERSION}/species/c_elegans/c_elegans.${WSVERSION}.annotations.gff3.gz c_elegans.current.genomic.fa.gz

