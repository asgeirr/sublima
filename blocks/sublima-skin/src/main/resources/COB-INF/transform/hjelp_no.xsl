<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns="http://www.w3.org/1999/xhtml"
        version="1.0">
    <xsl:template name="hjelp_no">
        <h3>A-Å emneordslister</h3>
	
        <ul>
			<li>Klikk på bokstavene for å velge emneord fra alfabetisk liste.</li>
		</ul>
		
		
		<h3>Søkefunksjoner</h3>
        <ul>
			<li>Søk på frase ved å sette søkeuttrykket mellom hermetegn. Eks. <i>"norsk språk"</i></li>
			<li>Enkelt søk bruker automatisk trunkering av søkeord, slik at søk på <i>film</i> også gir treff på <i>filmmusikk</i>. 
			Marker «Ikke-trunkert søk» hvis du bare vil ha eksakte treff.</li>
			<li>Autofullfør-funksjonen henter søkeforslag fra portalens emneordsliste. Velg søkeord ved å klikke i nedtrekkslisten, 
			eller ved å navigere med piltastene og velge med retur-tasten.</li>
			<li>Søk på mer enn ett søkeord vil normalt gi treff i emner og ressurser som inneholder alle ordene. 
			Hvis du markerer ELLER, vil du få treff i alle emner og ressurser som inneholder minst ett av ordene.</li>
			<li>Sorteringsrekkefølge må velges <u>før</u> du utfører et søk.</li>
			<li>Velg avansert søk hvis du vil gjøre mer nøyaktige feltavhengige søk, eller hvis du også vil søke i ressursenes
			innhold. Avansert søk gir søkeforslag med autofullfør for emner og utgivere.</li>
		</ul>
		
		
		<h3>Resultater</h3>
        <ul>
			<li>Filtrer søket ved å velge avgrensningskriterier i feltet til venstre på skjermen (Avgrens søk).</li>
			<li>Velg emneord fra feltet til høyre på skjermen for å se alle ressurser som er indeksert med det aktuelle emnet, 
			og for å navigere videre til beslektede emner.</li>
		</ul>
		
		
		<h3>Skriftstørrelse</h3>
        <ul>
			<li>Velg skriftstørrelse øverst til høyre på siden (A A A).</li>
		</ul>
		
		
		<h3>API</h3>
		<p>Data i Detektor er lagret i RDF-format, hovedsakelig beskrevet ved hjelp av vokabularene SKOS og Dublin Core. 
		Direkte tilgang til data er mulig gjennom to grensesnitt:
			<ul>
				<li><u>SRU-server</u><br/>Detektor har enkel støtte for SRU.<br/>
				Eksempelsøk: <a href="http://detektor.emneportal.no/sublima/sruserver?operation=searchRetrieve&version=1.1&query=charles_darwin">
				http://detektor.emneportal.no/sublima/sruserver?operation=searchRetrieve&version=1.1&query=charles_darwin</a></li>
				<p/>
				<li><u>SPARQL endpoint</u><br/>RDF-data er tilgjengelig i flere formater via SPARQL endpoint: <a href=
				"http://detektor.emneportal.no:8890/sparql">http://detektor.emneportal.no:8890/sparql</a></li>
			</ul>
		</p>
    </xsl:template>
</xsl:stylesheet>

