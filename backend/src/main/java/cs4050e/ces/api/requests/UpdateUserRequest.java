package cs4050e.ces.api.requests;

/** Represents a request to update a user's information. */
public class UpdateUserRequest extends UserRequest {
    /** The user's new name. */
    public String name;

    /** The user's new last name. */
    public String lastName;

    /** The user's new mailing address. */
    public String mailingAddress;
} // UpdateUserRequest
