package org.intermine.bio.util;

/*
 * Copyright (C) 2002-2011 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */


/**
 * A class to hold data about one organism.
 * @author Kim Rutherford
 */
public class OrganismData
{
    private String species;
    private String genus;
    private String abbreviation;
    private int taxonId;

    /**
     * Create a new OrganismData object.  Not public because these are created only by
     * OrganismRepository.
     */
    OrganismData() {
        // empty
    }

    /**
     * Get the short of this organism, eg. D. melanogaster
     * @return the genus
     */
    public String getShortName() {
        return genus.substring(0, 1) + ". " + species;
    }

    /**
     * Get the genus of this organism.
     * @return the genus
     */
    public String getGenus() {
        return genus;
    }

    /**
     * Get the species of this organism.
     * @return the species
     */
    public String getSpecies() {
        return species;
    }

    /**
     * Get the abbreviation of this organism.  eg. "Dmel"
     * @return the abbreviation
     */
    public String getAbbreviation() {
        return abbreviation;
    }

    /**
     * Get the taxonId of this organism.
     * @return the taxonId
     */
    public int getTaxonId() {
        return taxonId;
    }

    /**
     * Set the species of this organism.
     * @param species the species to set
     */
    void setSpecies(String species) {
        this.species = species;
    }

    /**
     * Set the genus of this organism.
     * @param genus the genus to set
     */
    void setGenus(String genus) {
        this.genus = genus;
    }

    /**
     * Set the abbreviation of this organism.  eg. "Dmel"
     * @param abbreviation the abbreviation to set
     */
    void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    /**
     * Set the taxonId of this organism.
     * @param taxonId the taxonId to set
     */
    void setTaxonId(int taxonId) {
        this.taxonId = taxonId;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return genus + " " + species + ", " + abbreviation + ", " + taxonId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof OrganismData && ((OrganismData) obj).toString().equals(toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}