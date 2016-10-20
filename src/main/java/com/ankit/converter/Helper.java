package com.ankit.converter;

import com.googlecode.protobuf.format.JsonFormat;
import com.nxl.navigo.api.proto.common.Request;
import com.nxl.navigo.api.proto.common.Response;
import com.nxl.navigo.api.proto.search.SearchResponse;
import com.nxl.navigo.api.proto.search.flight.FlightItinerary;
import org.apache.commons.lang3.StringUtils;
import org.iata.iata.edist.*;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.math.BigDecimal;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Ankit on 2016/10/18.
 */
public class Helper {
    /**
     * Created by Ankit...
     * @param response
     * @return
     */
    public AirShoppingRS generateXMLResponse(SearchResponse response) {
        AirShoppingRS res = new AirShoppingRS();
        res.withSuccess(new SuccessType()).withAirShoppingProcessing(new OrdViewProcessType()
                .withRemarks(new RemarkType().withRemark(new RemarkType.Remark().withValue("SEARCH"))));
        res.withTimeStamp(convertTOXMLGregorialCalender(response.getTimestamp())).withCorrelationID("SABRE");
        res.withShoppingResponseID(new ShoppingResponseIDType()
                .withOwner(response.getResponseHolder(0).getOriginDestination(0).getCommon(0).getInfo()));
        res.withOffersGroup(new AirShoppingRS.OffersGroup().withAllOffersSnapshot(new AirlineOffersSnapshotType()
                .withLowest(new AirlineOffersSnapshotType.Lowest()
                        .withEncodedCurrencyPrice(new EncodedPriceType()
                                .withValue(BigDecimal.valueOf(response.getResponseHolder(0).getOriginDestination(0)
                                        .getCommon(0).getDayLowestFare()))))));
        //Populate a map of OD(s) for further use...
        Map<String, SearchResponse.OriginDestinationInfo> mapOD = new LinkedHashMap<>();
        int odCounter = 0;
        for (SearchResponse.OriginDestinationInfo odInfo:
             response.getResponseHolder(0).getOriginDestinationList()) {
            mapOD.put(String.valueOf(odCounter), odInfo);
            odCounter++;
        }
        //Populating ODInfo...
        res.withDataLists(new AirShoppingRS.DataLists()
                .withOriginDestinationList(new DataListType.OriginDestinationList()));
        int counter = 0;
        for (String key:
             mapOD.keySet()) {
            System.out.println("key: " + key);
            SearchResponse.OriginDestinationInfo odInfo = mapOD.get(key);
            int fiCounter = 0;
            for (FlightItinerary fi:
                 odInfo.getFlightItineraryList()) {
                System.out.println("ficounter: " + fiCounter);
                res.getDataLists().getOriginDestinationList()
                        .withOriginDestination(new OriginDestination()
                                .withOriginDestinationKey(String.valueOf(fiCounter)));
                fiCounter++;
            }
            System.out.println("Counter: " + counter);
            counter++;
        }
        return res;
    }

    public XMLGregorianCalendar convertStringToXMLGregorialCalendar(String departureDate) {
        XMLGregorianCalendar xmlGregorialCalendar = null;
        try {
            if(!StringUtils.isEmpty(departureDate)) {
                xmlGregorialCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(departureDate);
            }
            else {
                return null;
            }
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        return xmlGregorialCalendar;
    }

    public XMLGregorianCalendar convertTOXMLGregorialCalender(long date) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(date);

        XMLGregorianCalendar xmlGregorialCalendar = null;
        try {
            xmlGregorialCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        return xmlGregorialCalendar;
    }
}
