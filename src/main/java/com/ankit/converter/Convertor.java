package com.ankit.converter;

import com.google.protobuf.ExtensionRegistry;
import com.googlecode.protobuf.format.JsonFormat;
import com.nxl.navigo.api.proto.common.Request;
import com.nxl.navigo.api.proto.common.Response;
import com.nxl.navigo.api.proto.search.SearchRequest;
import com.nxl.navigo.api.proto.search.SearchResponse;
import com.nxl.navigo.api.proto.types.PassengerType;
import org.apache.commons.lang3.StringUtils;
import org.iata.iata.edist.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ankit on 2016/10/17.
 */
public class Convertor {
    public static void main(String[] args) {
        /*String requestJson = "{   \"type\": \"SEARCH\",   \"navigo.SearchRequest.request\": {     \"passenger\": [       {         \"type\": \"ADT\",         \"quantity\": 2       },       {         \"type\": \"CHD\",         \"quantity\": 2       },       {         \"type\": \"INF\",         \"quantity\": 2       }     ],     \"flightSearch\": [       {         \"departureAirport\": \"YUL\",         \"departureDate\": \"2016-11-22\",         \"arrivalAirport\": \"FRA\",         \"sequence\": 1       },       {         \"departureAirport\": \"MUC\",         \"departureDate\": \"2016-11-29\",         \"arrivalAirport\": \"YUL\",         \"sequence\": 1       }     ],     \"tripType\": \"OJ\",     \"currencyCode\": \"CAD\",     \"travelClass\": \"ECONOMY\"   } }";
        String requestXML = convertJsonTOXML(requestJson);*/
        String responseJson = "";
        String responseXML = convertJSONTOXMLResponse(responseJson);
    }

    /**
     * Created by Ankit...
     * @param responseJson
     * @return
     */
    private static String convertJSONTOXMLResponse(String responseJson) {
        ExtensionRegistry registry = ExtensionRegistry.newInstance();
        registry.add(SearchResponse.response);
        Response.Builder builder = Response.newBuilder();
        try {
            FileReader fr = new FileReader(new File("SabreJson_RS.json"));
            JsonFormat.merge(fr, registry, builder);
            System.out.println("json" + builder.getType());
            Helper helper = new Helper();
            AirShoppingRS res = helper.generateXMLResponse(builder.getExtension(SearchResponse.response));
            FileOutputStream fos = new FileOutputStream(new File("response.xml"));
            JAXBContext context = JAXBContext.newInstance(AirShoppingRS.class);
            context.createMarshaller().marshal(res, fos);
        } catch (JsonFormat.ParseException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        catch(FileNotFoundException fne) {
            fne.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Created by Ankit...
     * @param requestJson
     * @return
     */
    private static String convertJsonTOXML(String requestJson) {
        ExtensionRegistry registry = ExtensionRegistry.newInstance();
        registry.add(SearchRequest.request);
        Request.Builder builder = Request.newBuilder();
        try {
            JsonFormat.merge(requestJson, registry, builder);
            System.out.println("json" + builder.getType());
            AirShoppingRQ req = generateXML(builder);
            FileOutputStream fos = new FileOutputStream(new File("request.xml"));
            JAXBContext context = JAXBContext.newInstance(AirShoppingRQ.class);
            context.createMarshaller().marshal(req, fos);
        } catch (JsonFormat.ParseException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        catch(FileNotFoundException fne) {
            fne.printStackTrace();
        }
        return null;
    }

    /**
     * Created by Ankit...
     * @param builder
     * @return
     */
    private static AirShoppingRQ generateXML(Request.Builder builder) {
        AirShoppingRQ req = new AirShoppingRQ();
        SearchRequest searchRQ = builder.getExtension(SearchRequest.request);
        req.withTravelers(new Travelers());
        //Adding pax list...
        for (SearchRequest.Passenger pax:
             searchRQ.getPassengerList()) {
            req.getTravelers().withTraveler(new Travelers.Traveler().withAnonymousTraveler(new AnonymousTravelerType()
                    .withPTC(new TravelerCoreType.PTC().withValue(pax.getType().toString())
                            .withQuantity(new BigInteger(String.valueOf(pax.getQuantity()))))));
        }
        //Adding Segment list...
        Helper helper = new Helper();
        req.withCoreQuery(new AirShoppingRQ.CoreQuery().withOriginDestinations(new AirShopReqAttributeQueryType()));
        for (SearchRequest.FlightSearch fs:
             searchRQ.getFlightSearchList()) {
            req.getCoreQuery().getOriginDestinations()
                    .withOriginDestination(new AirShopReqAttributeQueryType.OriginDestination()
                            .withDeparture(new Departure()
                                    .withDate(helper.convertStringToXMLGregorialCalendar(fs.getDepartureDate()))
                                    .withAirportCode(new FlightDepartureType.AirportCode()
                                            .withValue(fs.getDepartureAirport())))
                            .withArrival(new FlightArrivalType().withAirportCode(new FlightArrivalType.AirportCode()
                                    .withValue(fs.getArrivalAirport()))));
        }
        req.withMetadata(new AirShoppingRQ.Metadata()
                .withOther(new AirShopReqMetadataType.Other()
                .withOtherMetadata(new AirShopReqMetadataType.Other.OtherMetadata()
                        .withCurrencyMetadatas(new CurrencyMetadatas().withCurrencyMetadata(new CurrencyMetadata()
                                .withName(searchRQ.getCurrencyCode().toString())))))
                .withShopping(new AirShopReqMetadataType.Shopping().withShopMetadataGroup(new ShopMetadataGroup()
                        .withLocation(new Location().withDirectionMetadatas(new DirectionMetadatas()
                                .withDirectionMetadata(new DirectionsMetadataType()
                                        .withAugmentationPoint(new AugPointInfoType()
                                                .withAugPoint(new AugPointType()
                                                        .withTripType(searchRQ.getTripType().toString())))))))))
                .withPreference(new AirShoppingRQ.Preference().withCabinPreferences(new CabinPreferencesType()
                        .withCabinType(new CabinType().withName(searchRQ.getTravelClass().toString()))));
        return  req;
    }

    /**
     * Created by Ankit...
     * @param departureDate
     * @return
     */

}
